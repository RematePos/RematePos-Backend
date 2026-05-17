package com.corhuila.microservices.auth_microservice.service;

import com.corhuila.microservices.auth_microservice.dto.RegisterRequest;
import com.corhuila.microservices.auth_microservice.dto.UserSessionResponse;
import com.corhuila.microservices.auth_microservice.model.Permission;
import com.corhuila.microservices.auth_microservice.model.Role;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.repository.RoleRepository;
import com.corhuila.microservices.auth_microservice.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

    public UserSessionResponse toSessionResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new UserSessionResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roles,
                permissions
        );
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no registrado: " + roleName)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
