package com.corhuila.microservices.auth_microservice.service;

import com.corhuila.microservices.auth_microservice.dto.CreateTenantUserRequest;
import com.corhuila.microservices.auth_microservice.dto.TenantUserResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantUserStatusResponse;
import com.corhuila.microservices.auth_microservice.dto.UpdateTenantUserRequest;
import com.corhuila.microservices.auth_microservice.model.Permission;
import com.corhuila.microservices.auth_microservice.model.Role;
import com.corhuila.microservices.auth_microservice.model.RoleScope;
import com.corhuila.microservices.auth_microservice.model.Tenant;
import com.corhuila.microservices.auth_microservice.model.TenantMembership;
import com.corhuila.microservices.auth_microservice.model.TenantStatus;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.repository.RoleRepository;
import com.corhuila.microservices.auth_microservice.repository.TenantMembershipRepository;
import com.corhuila.microservices.auth_microservice.repository.TenantRepository;
import com.corhuila.microservices.auth_microservice.repository.UserRepository;
import java.util.Comparator;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantUserService {

    private static final String PLATFORM_SUPER_ADMIN_ROLE = "PLATFORM_SUPER_ADMIN";
    private static final String BUSINESS_OWNER_ROLE = "BUSINESS_OWNER";
    private static final Set<String> TENANT_ASSIGNABLE_ROLES = Set.of("CASHIER", "BUSINESS_ADMIN");
    private static final Set<String> READ_PERMISSIONS = Set.of("USERS_CREATE", "USERS_UPDATE");

    private final TenantRepository tenantRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantUserService(
            TenantRepository tenantRepository,
            TenantMembershipRepository tenantMembershipRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantMembershipRepository = tenantMembershipRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TenantUserResponse createTenantUser(Long tenantId, CreateTenantUserRequest request, User currentUser) {
        Tenant tenant = findActiveTenant(tenantId);
        authorize(currentUser, tenantId, "USERS_CREATE");

        String username = normalize(request.username());
        String email = normalize(request.email());
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }

        Role role = resolveAssignableTenantRole(request.role());

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(resolveFullName(request.fullName(), username));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user = userRepository.save(user);

        TenantMembership membership = new TenantMembership();
        membership.setUser(user);
        membership.setTenant(tenant);
        membership.setRole(role);
        membership.setActive(true);
        membership = tenantMembershipRepository.save(membership);

        return toResponse(membership);
    }

    @Transactional(readOnly = true)
    public java.util.List<TenantUserResponse> listTenantUsers(Long tenantId, User currentUser) {
        findActiveTenant(tenantId);
        authorizeAny(currentUser, tenantId, READ_PERMISSIONS);

        return tenantMembershipRepository.findByTenantId(tenantId).stream()
                .sorted(Comparator.comparing(membership -> membership.getUser().getId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TenantUserResponse updateTenantUser(Long tenantId, Long userId, UpdateTenantUserRequest request, User currentUser) {
        findActiveTenant(tenantId);
        authorize(currentUser, tenantId, "USERS_UPDATE");

        TenantMembership membership = findMembership(tenantId, userId);
        User user = membership.getUser();

        if (request.email() != null && !request.email().isBlank()) {
            String email = normalize(request.email());
            boolean belongsToOtherUser = userRepository.findByEmail(email)
                    .map(existing -> !existing.getId().equals(user.getId()))
                    .orElse(false);
            if (belongsToOtherUser) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
            }
            user.setEmail(email);
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        if (request.role() != null && !request.role().isBlank()) {
            Role role = resolveAssignableTenantRole(request.role());
            ensureNotDemotingOnlyActiveOwner(membership, role.getName());
            membership.setRole(role);
        }

        if (request.active() != null) {
            if (Boolean.FALSE.equals(request.active())) {
                ensureNotDisablingOnlyActiveOwner(membership);
            }
            membership.setActive(request.active());
        }

        user.setEnabled(shouldUserRemainEnabled(user, membership));
        userRepository.save(user);
        return toResponse(tenantMembershipRepository.save(membership));
    }

    @Transactional
    public TenantUserStatusResponse disableTenantUser(Long tenantId, Long userId, User currentUser) {
        findActiveTenant(tenantId);
        authorize(currentUser, tenantId, "USERS_UPDATE");

        TenantMembership membership = findMembership(tenantId, userId);
        ensureNotDisablingOnlyActiveOwner(membership);
        membership.setActive(false);
        tenantMembershipRepository.save(membership);

        User user = membership.getUser();
        user.setEnabled(shouldUserRemainEnabled(user, membership));
        userRepository.save(user);

        return toStatusResponse(membership);
    }

    @Transactional
    public TenantUserStatusResponse enableTenantUser(Long tenantId, Long userId, User currentUser) {
        findActiveTenant(tenantId);
        authorize(currentUser, tenantId, "USERS_UPDATE");

        TenantMembership membership = findMembership(tenantId, userId);
        membership.setActive(true);
        tenantMembershipRepository.save(membership);

        User user = membership.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return toStatusResponse(membership);
    }

    private Tenant findActiveTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .filter(tenant -> TenantStatus.ACTIVE.equals(tenant.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found."));
    }

    private TenantMembership findMembership(Long tenantId, Long userId) {
        return tenantMembershipRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant user not found."));
    }

    private void authorize(User currentUser, Long tenantId, String permission) {
        if (isPlatformSuperAdmin(currentUser)) {
            return;
        }

        TenantMembership membership = tenantMembershipRepository
                .findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(currentUser.getId(), tenantId, TenantStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant access is denied."));

        if (!hasPermission(membership, permission)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Required permission is missing.");
        }
    }

    private void authorizeAny(User currentUser, Long tenantId, Set<String> permissions) {
        if (isPlatformSuperAdmin(currentUser)) {
            return;
        }

        TenantMembership membership = tenantMembershipRepository
                .findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(currentUser.getId(), tenantId, TenantStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant access is denied."));

        boolean allowed = permissions.stream().anyMatch(permission -> hasPermission(membership, permission));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Required permission is missing.");
        }
    }

    private boolean hasPermission(TenantMembership membership, String permission) {
        return membership.getRole().getPermissions().stream()
                .map(Permission::getName)
                .anyMatch(permission::equals);
    }

    private boolean isPlatformSuperAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> PLATFORM_SUPER_ADMIN_ROLE.equals(role.getName()) && RoleScope.PLATFORM.equals(role.getScope()));
    }

    private Role resolveAssignableTenantRole(String roleName) {
        String normalizedRole = normalizeRole(roleName);
        if (!TENANT_ASSIGNABLE_ROLES.contains(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is not allowed for tenant user management.");
        }
        return roleRepository.findByName(normalizedRole)
                .filter(role -> RoleScope.TENANT.equals(role.getScope()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, normalizedRole + " role is not configured."));
    }

    private void ensureNotDisablingOnlyActiveOwner(TenantMembership membership) {
        if (BUSINESS_OWNER_ROLE.equals(membership.getRole().getName()) && membership.isActive()
                && tenantMembershipRepository.countByTenantIdAndRoleNameAndActiveTrue(membership.getTenant().getId(), BUSINESS_OWNER_ROLE) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The only active business owner cannot be disabled.");
        }
    }

    private void ensureNotDemotingOnlyActiveOwner(TenantMembership membership, String nextRole) {
        if (!BUSINESS_OWNER_ROLE.equals(nextRole)) {
            ensureNotDisablingOnlyActiveOwner(membership);
        }
    }

    private boolean shouldUserRemainEnabled(User user, TenantMembership changedMembership) {
        if (user.getRoles().stream().anyMatch(role -> RoleScope.PLATFORM.equals(role.getScope()))) {
            return true;
        }
        return tenantMembershipRepository.findByUserIdAndActiveTrueAndTenantStatus(user.getId(), TenantStatus.ACTIVE).stream()
                .anyMatch(membership -> !membership.getId().equals(changedMembership.getId()) || changedMembership.isActive());
    }

    private TenantUserResponse toResponse(TenantMembership membership) {
        User user = membership.getUser();
        Tenant tenant = membership.getTenant();
        return new TenantUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                tenant.getId(),
                tenant.getSlug(),
                membership.getRole().getName(),
                membership.isActive()
        );
    }

    private TenantUserStatusResponse toStatusResponse(TenantMembership membership) {
        return new TenantUserStatusResponse(
                membership.getUser().getId(),
                membership.getTenant().getId(),
                membership.getRole().getName(),
                membership.isActive()
        );
    }

    private String resolveFullName(String fullName, String username) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName.trim();
        }
        return username;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeRole(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
