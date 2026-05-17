package com.corhuila.microservices.auth_microservice.service;

import com.corhuila.microservices.auth_microservice.dto.LoginRequest;
import com.corhuila.microservices.auth_microservice.dto.LoginResponse;
import com.corhuila.microservices.auth_microservice.dto.RegisterRequest;
import com.corhuila.microservices.auth_microservice.dto.UserSessionResponse;
import com.corhuila.microservices.auth_microservice.model.Role;
import com.corhuila.microservices.auth_microservice.model.RoleScope;
import com.corhuila.microservices.auth_microservice.model.TenantMembership;
import com.corhuila.microservices.auth_microservice.model.TenantStatus;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.repository.TenantMembershipRepository;
import com.corhuila.microservices.auth_microservice.repository.UserRepository;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final boolean demoRegistrationEnabled;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            TenantMembershipRepository tenantMembershipRepository,
            JwtService jwtService,
            UserService userService,
            @Value("${auth.demo-registration-enabled:false}") boolean demoRegistrationEnabled
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tenantMembershipRepository = tenantMembershipRepository;
        this.jwtService = jwtService;
        this.userService = userService;
        this.demoRegistrationEnabled = demoRegistrationEnabled;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        List<TenantMembership> memberships = tenantMembershipRepository.findByUserIdAndActiveTrueAndTenantStatus(
                user.getId(),
                TenantStatus.ACTIVE
        );

        if (request.tenantId() != null) {
            TenantMembership membership = tenantMembershipRepository.findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(
                    user.getId(),
                    request.tenantId(),
                    TenantStatus.ACTIVE
            ).orElseThrow(() -> new BadCredentialsException("El usuario no pertenece al negocio solicitado"));
            return issueTenantToken(user, membership);
        }

        if (memberships.size() == 1) {
            return issueTenantToken(user, memberships.get(0));
        }

        if (memberships.size() > 1) {
            return new LoginResponse(
                    null,
                    "Bearer",
                    0,
                    "TENANT_SELECTION_REQUIRED",
                    userService.toSessionResponse(user)
            );
        }

        if (hasPlatformSuperAdminRole(user)) {
            String token = jwtService.generateToken(user);
            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationMs(),
                    "AUTHENTICATED",
                    userService.toSessionResponse(user)
            );
        }

        throw new BadCredentialsException("El usuario no tiene membresias activas");
    }

    @Transactional(readOnly = true)
    public UserSessionResponse currentUser(String username) {
        return currentUser(username, null);
    }

    @Transactional(readOnly = true)
    public UserSessionResponse currentUser(String username, Long tenantId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        if (tenantId == null) {
            return userService.toSessionResponse(user);
        }
        TenantMembership membership = tenantMembershipRepository.findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(
                user.getId(),
                tenantId,
                TenantStatus.ACTIVE
        ).orElseThrow(() -> new BadCredentialsException("El usuario no pertenece al negocio solicitado"));
        return userService.toSessionResponse(user, membership);
    }

    @Transactional
    public UserSessionResponse registerDemoUser(RegisterRequest request) {
        if (!demoRegistrationEnabled) {
            throw new IllegalStateException("El registro demo esta deshabilitado");
        }
        User user = userService.createDemoTenantUser(request, Set.of("CASHIER"));
        return userService.toSessionResponse(user);
    }

    public Long extractTenantId(String token) {
        return jwtService.extractTenantId(token);
    }

    private LoginResponse issueTenantToken(User user, TenantMembership membership) {
        String token = jwtService.generateToken(user, membership);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                "AUTHENTICATED",
                userService.toSessionResponse(user, membership)
        );
    }

    private boolean hasPlatformSuperAdminRole(User user) {
        return user.getRoles().stream()
                .filter(role -> RoleScope.PLATFORM.equals(role.getScope()))
                .map(Role::getName)
                .anyMatch("PLATFORM_SUPER_ADMIN"::equals);
    }
}
