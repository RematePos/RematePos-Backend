package com.corhuila.microservices.auth_microservice.service;

import com.corhuila.microservices.auth_microservice.dto.RegisterRequest;
import com.corhuila.microservices.auth_microservice.dto.TenantContextResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantMembershipResponse;
import com.corhuila.microservices.auth_microservice.dto.UserSessionResponse;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantMembershipRepository tenantMembershipRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantMembershipRepository = tenantMembershipRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(RegisterRequest request, Set<String> allowedRoles) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo ya existe");
        }

        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty()
                ? Set.of("CASHIER")
                : request.roles();
        if (!allowedRoles.containsAll(requestedRoles)) {
            throw new IllegalArgumentException("La solicitud contiene roles no permitidos");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(resolveRoles(requestedRoles));

        return userRepository.save(user);
    }

    @Transactional
    public User createDemoTenantUser(RegisterRequest request, Set<String> allowedRoles) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo ya existe");
        }

        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty()
                ? Set.of("CASHIER")
                : request.roles();
        if (!allowedRoles.containsAll(requestedRoles) || requestedRoles.size() != 1) {
            throw new IllegalArgumentException("La solicitud contiene roles no permitidos");
        }

        Role role = resolveTenantRole(requestedRoles.iterator().next());
        Tenant tenant = tenantRepository.findBySlug("rematepos-demo").orElseGet(() -> {
            Tenant demoTenant = new Tenant();
            demoTenant.setName("RematePOS Demo Store");
            demoTenant.setSlug("rematepos-demo");
            demoTenant.setStatus(TenantStatus.ACTIVE);
            return tenantRepository.save(demoTenant);
        });

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user = userRepository.save(user);

        TenantMembership membership = new TenantMembership();
        membership.setUser(user);
        membership.setTenant(tenant);
        membership.setRole(role);
        membership.setActive(true);
        tenantMembershipRepository.save(membership);

        return user;
    }

    public UserSessionResponse toSessionResponse(User user) {
        return toSessionResponse(user, null);
    }

    public UserSessionResponse toSessionResponse(User user, TenantMembership activeMembership) {
        List<TenantMembership> memberships = tenantMembershipRepository.findByUserIdAndActiveTrueAndTenantStatus(
                user.getId(),
                TenantStatus.ACTIVE
        );
        Set<String> platformRoles = user.getRoles().stream()
                .filter(role -> RoleScope.PLATFORM.equals(role.getScope()))
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> roles = activeMembership == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(Set.of(activeMembership.getRole().getName()));
        Set<String> permissions = new LinkedHashSet<>();
        if (activeMembership != null) {
            activeMembership.getRole().getPermissions().stream()
                    .map(Permission::getName)
                    .forEach(permissions::add);
        }
        return new UserSessionResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                activeMembership == null ? null : new TenantContextResponse(
                        activeMembership.getTenant().getId(),
                        activeMembership.getTenant().getName(),
                        activeMembership.getTenant().getSlug()
                ),
                roles,
                permissions,
                platformRoles,
                memberships.stream()
                        .map(membership -> new TenantMembershipResponse(
                                membership.getTenant().getId(),
                                membership.getTenant().getName(),
                                membership.getTenant().getSlug(),
                                membership.getRole().getName()
                        ))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no registrado: " + roleName)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Role resolveTenantRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Rol no registrado: " + roleName));
        if (!RoleScope.TENANT.equals(role.getScope())) {
            throw new IllegalArgumentException("El rol solicitado no es valido para un negocio");
        }
        return role;
    }
}
