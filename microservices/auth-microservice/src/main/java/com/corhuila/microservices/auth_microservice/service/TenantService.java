package com.corhuila.microservices.auth_microservice.service;

import com.corhuila.microservices.auth_microservice.dto.CreateTenantOwnerRequest;
import com.corhuila.microservices.auth_microservice.dto.CreateTenantRequest;
import com.corhuila.microservices.auth_microservice.dto.CreateTenantResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantStatusUpdateResponse;
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
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantService {

    private static final String BUSINESS_OWNER_ROLE = "BUSINESS_OWNER";
    private static final String PLATFORM_SUPER_ADMIN_ROLE = "PLATFORM_SUPER_ADMIN";

    private final TenantRepository tenantRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(
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
    public CreateTenantResponse createTenantWithOwner(CreateTenantRequest request) {
        String slug = normalize(request.slug());
        CreateTenantOwnerRequest ownerRequest = request.owner();
        String ownerUsername = normalize(ownerRequest.username());
        String ownerEmail = normalize(ownerRequest.email());

        if (tenantRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant slug already exists.");
        }
        if (userRepository.existsByUsername(ownerUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner username already exists.");
        }
        if (userRepository.existsByEmail(ownerEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner email already exists.");
        }

        Role businessOwner = roleRepository.findByName(BUSINESS_OWNER_ROLE)
                .filter(role -> RoleScope.TENANT.equals(role.getScope()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "BUSINESS_OWNER role is not configured."));

        Tenant tenant = new Tenant();
        tenant.setName(request.name().trim());
        tenant.setSlug(slug);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant = tenantRepository.save(tenant);

        User owner = new User();
        owner.setUsername(ownerUsername);
        owner.setEmail(ownerEmail);
        owner.setFullName(resolveOwnerFullName(ownerRequest));
        owner.setPasswordHash(passwordEncoder.encode(ownerRequest.password()));
        owner.setEnabled(true);
        owner = userRepository.save(owner);

        TenantMembership membership = new TenantMembership();
        membership.setUser(owner);
        membership.setTenant(tenant);
        membership.setRole(businessOwner);
        membership.setActive(true);
        tenantMembershipRepository.save(membership);

        return new CreateTenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus().name(),
                owner.getId(),
                owner.getUsername(),
                businessOwner.getName()
        );
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> listTenantsForCurrentUser(User currentUser) {
        if (isPlatformSuperAdmin(currentUser)) {
            return tenantRepository.findAll().stream()
                    .sorted(Comparator.comparing(Tenant::getId))
                    .map(this::toTenantResponse)
                    .toList();
        }

        return tenantMembershipRepository.findByUserIdAndActiveTrueAndTenantStatus(currentUser.getId(), TenantStatus.ACTIVE).stream()
                .map(TenantMembership::getTenant)
                .sorted(Comparator.comparing(Tenant::getId))
                .map(this::toTenantResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(Long tenantId, User currentUser) {
        if (isPlatformSuperAdmin(currentUser)) {
            return tenantRepository.findById(tenantId)
                    .map(this::toTenantResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found."));
        }

        return tenantMembershipRepository
                .findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(currentUser.getId(), tenantId, TenantStatus.ACTIVE)
                .map(TenantMembership::getTenant)
                .map(this::toTenantResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found."));
    }

    @Transactional
    public TenantStatusUpdateResponse suspendTenant(Long tenantId) {
        Tenant tenant = findTenant(tenantId);
        tenant.setStatus(TenantStatus.SUSPENDED);
        return toStatusResponse(tenantRepository.save(tenant));
    }

    @Transactional
    public TenantStatusUpdateResponse activateTenant(Long tenantId) {
        Tenant tenant = findTenant(tenantId);
        tenant.setStatus(TenantStatus.ACTIVE);
        return toStatusResponse(tenantRepository.save(tenant));
    }

    private Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found."));
    }

    private boolean isPlatformSuperAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> PLATFORM_SUPER_ADMIN_ROLE.equals(role.getName()) && RoleScope.PLATFORM.equals(role.getScope()));
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus().name()
        );
    }

    private TenantStatusUpdateResponse toStatusResponse(Tenant tenant) {
        return new TenantStatusUpdateResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus().name()
        );
    }

    private String resolveOwnerFullName(CreateTenantOwnerRequest ownerRequest) {
        if (ownerRequest.fullName() != null && !ownerRequest.fullName().isBlank()) {
            return ownerRequest.fullName().trim();
        }
        return ownerRequest.username().trim();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
