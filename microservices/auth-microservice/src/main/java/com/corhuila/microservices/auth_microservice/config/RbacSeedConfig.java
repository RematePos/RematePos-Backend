package com.corhuila.microservices.auth_microservice.config;

import com.corhuila.microservices.auth_microservice.model.Permission;
import com.corhuila.microservices.auth_microservice.model.Role;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.repository.PermissionRepository;
import com.corhuila.microservices.auth_microservice.repository.RoleRepository;
import com.corhuila.microservices.auth_microservice.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class RbacSeedConfig {

    private static final List<String> ALL_PERMISSIONS = List.of(
            "SALES_READ",
            "SALES_CREATE",
            "PRODUCTS_READ",
            "PRODUCTS_CREATE",
            "PRODUCTS_UPDATE",
            "CATEGORIES_READ",
            "CATEGORIES_CREATE",
            "CATEGORIES_UPDATE",
            "CUSTOMERS_READ",
            "CUSTOMERS_CREATE",
            "INVOICES_READ",
            "RETURNS_CREATE",
            "CASH_REGISTER_READ",
            "CASH_REGISTER_MANAGE",
            "ACCOUNT_MANAGE"
    );

    private static final Set<String> CASHIER_PERMISSIONS = Set.of(
            "SALES_READ",
            "SALES_CREATE",
            "PRODUCTS_READ",
            "CATEGORIES_READ",
            "CUSTOMERS_READ",
            "CUSTOMERS_CREATE",
            "INVOICES_READ",
            "RETURNS_CREATE",
            "CASH_REGISTER_READ"
    );

    private static final Set<String> QA_SUPPORT_PERMISSIONS = Set.of(
            "SALES_READ",
            "PRODUCTS_READ",
            "CATEGORIES_READ",
            "CUSTOMERS_READ",
            "INVOICES_READ",
            "CASH_REGISTER_READ"
    );

    @Bean
    ApplicationRunner seedRbac(
            RbacSeeder seeder,
            @Value("${auth.seed-demo-users:false}") boolean seedDemoUsers,
            @Value("${auth.demo-admin-password:}") String adminPassword,
            @Value("${auth.demo-cashier-password:}") String cashierPassword
    ) {
        return args -> seeder.seed(seedDemoUsers, adminPassword, cashierPassword);
    }

    @Configuration
    static class RbacSeeder {

        private final PermissionRepository permissionRepository;
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        RbacSeeder(
                PermissionRepository permissionRepository,
                RoleRepository roleRepository,
                UserRepository userRepository,
                PasswordEncoder passwordEncoder
        ) {
            this.permissionRepository = permissionRepository;
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public void seed(boolean seedDemoUsers, String adminPassword, String cashierPassword) {
            Map<String, Permission> permissions = seedPermissions();
            Role admin = seedRole("ADMIN", "Administrador RematePOS", ALL_PERMISSIONS, permissions);
            Role cashier = seedRole("CASHIER", "Cajero RematePOS", CASHIER_PERMISSIONS, permissions);
            seedRole("QA_SUPPORT", "Soporte QA RematePOS", QA_SUPPORT_PERMISSIONS, permissions);

            if (seedDemoUsers) {
                seedDemoUser("admin.demo", "admin.demo@rematepos.local", "Admin Demo", adminPassword, admin);
                seedDemoUser("cashier.demo", "cashier.demo@rematepos.local", "Cashier Demo", cashierPassword, cashier);
            }
        }

        private Map<String, Permission> seedPermissions() {
            ALL_PERMISSIONS.forEach(name -> permissionRepository.findByName(name).orElseGet(() -> {
                Permission permission = new Permission();
                permission.setName(name);
                permission.setDescription(name.replace('_', ' '));
                return permissionRepository.save(permission);
            }));

            return permissionRepository.findAll().stream()
                    .collect(Collectors.toMap(Permission::getName, Function.identity()));
        }

        private Role seedRole(
                String name,
                String description,
                Iterable<String> permissionNames,
                Map<String, Permission> permissions
        ) {
            Role role = roleRepository.findByName(name).orElseGet(Role::new);
            role.setName(name);
            role.setDescription(description);
            Set<Permission> rolePermissions = new LinkedHashSet<>();
            permissionNames.forEach(permissionName -> rolePermissions.add(permissions.get(permissionName)));
            role.setPermissions(rolePermissions);
            return roleRepository.save(role);
        }

        private void seedDemoUser(String username, String email, String fullName, String password, Role role) {
            if (password == null || password.isBlank() || userRepository.existsByUsername(username)) {
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setRoles(new LinkedHashSet<>(Set.of(role)));
            userRepository.save(user);
        }
    }
}
