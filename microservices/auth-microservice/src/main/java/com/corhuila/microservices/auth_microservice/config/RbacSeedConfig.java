package com.corhuila.microservices.auth_microservice.config;

import com.corhuila.microservices.auth_microservice.model.Permission;
import com.corhuila.microservices.auth_microservice.model.Role;
import com.corhuila.microservices.auth_microservice.model.RoleScope;
import com.corhuila.microservices.auth_microservice.model.Tenant;
import com.corhuila.microservices.auth_microservice.model.TenantMembership;
import com.corhuila.microservices.auth_microservice.model.TenantStatus;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.repository.PermissionRepository;
import com.corhuila.microservices.auth_microservice.repository.RoleRepository;
import com.corhuila.microservices.auth_microservice.repository.TenantMembershipRepository;
import com.corhuila.microservices.auth_microservice.repository.TenantRepository;
import com.corhuila.microservices.auth_microservice.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class RbacSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(RbacSeedConfig.class);

    private static final List<String> ALL_PERMISSIONS = List.of(
            "SALES_READ",
            "SALES_CREATE",
            "PRODUCTS_READ",
            "PRODUCTS_CREATE",
            "PRODUCTS_UPDATE",
            "PRODUCTS_DELETE",
            "CATEGORIES_READ",
            "CATEGORIES_CREATE",
            "CATEGORIES_UPDATE",
            "CUSTOMERS_READ",
            "CUSTOMERS_CREATE",
            "INVOICES_READ",
            "RETURNS_CREATE",
            "CASH_REGISTER_OPEN",
            "CASH_REGISTER_CLOSE",
            "CASH_REGISTER_READ",
            "CASH_REGISTER_MANAGE",
            "ACCOUNT_MANAGE",
            "TENANTS_CREATE",
            "TENANTS_READ",
            "TENANTS_SUSPEND",
            "USERS_CREATE",
            "USERS_UPDATE",
            "BUSINESS_SETTINGS_UPDATE",
            "REPORTS_READ"
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
            "CASH_REGISTER_OPEN",
            "CASH_REGISTER_CLOSE",
            "CASH_REGISTER_READ"
    );

    private static final Set<String> INVENTORY_MANAGER_PERMISSIONS = Set.of(
            "PRODUCTS_READ",
            "PRODUCTS_CREATE",
            "PRODUCTS_UPDATE",
            "PRODUCTS_DELETE",
            "CATEGORIES_READ",
            "CATEGORIES_CREATE",
            "CATEGORIES_UPDATE"
    );

    private static final Set<String> QA_SUPPORT_PERMISSIONS = Set.of(
            "SALES_READ",
            "PRODUCTS_READ",
            "CATEGORIES_READ",
            "CUSTOMERS_READ",
            "INVOICES_READ",
            "CASH_REGISTER_READ",
            "TENANTS_READ"
    );

    @Bean
    ApplicationRunner seedRbac(
            RbacSeeder seeder,
            @Value("${auth.seed-demo-users:false}") boolean seedDemoUsers,
            @Value("${auth.demo-admin-password:}") String adminPassword,
            @Value("${auth.demo-cashier-password:}") String cashierPassword,
            @Value("${auth.seed-platform-admin:false}") boolean seedPlatformAdmin,
            @Value("${auth.platform-admin-username:platform.admin}") String platformAdminUsername,
            @Value("${auth.platform-admin-password:}") String platformAdminPassword,
            @Value("${auth.platform-admin-email:platform.admin@example.com}") String platformAdminEmail
    ) {
        return args -> seeder.seed(
                seedDemoUsers,
                adminPassword,
                cashierPassword,
                seedPlatformAdmin,
                platformAdminUsername,
                platformAdminPassword,
                platformAdminEmail
        );
    }

    @Configuration
    static class RbacSeeder {

        private final PermissionRepository permissionRepository;
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final TenantRepository tenantRepository;
        private final TenantMembershipRepository tenantMembershipRepository;
        private final PasswordEncoder passwordEncoder;

        RbacSeeder(
                PermissionRepository permissionRepository,
                RoleRepository roleRepository,
                UserRepository userRepository,
                TenantRepository tenantRepository,
                TenantMembershipRepository tenantMembershipRepository,
                PasswordEncoder passwordEncoder
        ) {
            this.permissionRepository = permissionRepository;
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.tenantRepository = tenantRepository;
            this.tenantMembershipRepository = tenantMembershipRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public void seed(
                boolean seedDemoUsers,
                String adminPassword,
                String cashierPassword,
                boolean seedPlatformAdmin,
                String platformAdminUsername,
                String platformAdminPassword,
                String platformAdminEmail
        ) {
            Map<String, Permission> permissions = seedPermissions();
            backfillRoleScopes();
            seedRole("ADMIN", "Administrador RematePOS legado", RoleScope.TENANT, ALL_PERMISSIONS, permissions);
            Role businessOwner = seedRole("BUSINESS_OWNER", "Propietario del negocio", RoleScope.TENANT, ALL_PERMISSIONS, permissions);
            seedRole("BUSINESS_ADMIN", "Administrador del negocio", RoleScope.TENANT, ALL_PERMISSIONS, permissions);
            Role cashier = seedRole("CASHIER", "Cajero RematePOS", RoleScope.TENANT, CASHIER_PERMISSIONS, permissions);
            seedRole("INVENTORY_MANAGER", "Gestor de inventario", RoleScope.TENANT, INVENTORY_MANAGER_PERMISSIONS, permissions);
            Role platformSuperAdmin = seedRole("PLATFORM_SUPER_ADMIN", "Administrador de plataforma", RoleScope.PLATFORM, ALL_PERMISSIONS, permissions);
            seedRole("QA_SUPPORT", "Soporte QA RematePOS", RoleScope.PLATFORM, QA_SUPPORT_PERMISSIONS, permissions);

            if (seedDemoUsers) {
                Tenant demoTenant = seedDemoTenant();
                seedDemoUser("admin.demo", "admin.demo@rematepos.local", "Admin Demo", adminPassword, demoTenant, businessOwner);
                seedDemoUser("cashier.demo", "cashier.demo@rematepos.local", "Cashier Demo", cashierPassword, demoTenant, cashier);
            }

            if (seedPlatformAdmin) {
                seedPlatformAdmin(platformAdminUsername, platformAdminEmail, platformAdminPassword, platformSuperAdmin);
            }
        }

        private void backfillRoleScopes() {
            roleRepository.findAll().stream()
                    .filter(role -> role.getScope() == null)
                    .forEach(role -> {
                        if ("PLATFORM_SUPER_ADMIN".equals(role.getName()) || "QA_SUPPORT".equals(role.getName())) {
                            role.setScope(RoleScope.PLATFORM);
                        } else {
                            role.setScope(RoleScope.TENANT);
                        }
                        roleRepository.save(role);
                    });
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
                RoleScope scope,
                Iterable<String> permissionNames,
                Map<String, Permission> permissions
        ) {
            Role role = roleRepository.findByName(name).orElseGet(Role::new);
            role.setName(name);
            role.setDescription(description);
            role.setScope(scope);
            Set<Permission> rolePermissions = new LinkedHashSet<>();
            permissionNames.forEach(permissionName -> rolePermissions.add(permissions.get(permissionName)));
            role.setPermissions(rolePermissions);
            return roleRepository.save(role);
        }

        private Tenant seedDemoTenant() {
            Tenant tenant = tenantRepository.findBySlug("rematepos-demo").orElseGet(Tenant::new);
            tenant.setName("RematePOS Demo Store");
            tenant.setSlug("rematepos-demo");
            tenant.setStatus(TenantStatus.ACTIVE);
            return tenantRepository.save(tenant);
        }

        private void seedDemoUser(String username, String email, String fullName, String password, Tenant tenant, Role role) {
            if (password == null || password.isBlank()) {
                return;
            }

            User user = userRepository.findByUsername(username).orElseGet(User::new);
            if (user.getId() == null) {
                user.setUsername(username);
                user.setEmail(email);
                user.setFullName(fullName);
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setEnabled(true);
                user = userRepository.save(user);
            }

            if (tenantMembershipRepository.findByUserIdAndTenantId(user.getId(), tenant.getId()).isEmpty()) {
                TenantMembership membership = new TenantMembership();
                membership.setUser(user);
                membership.setTenant(tenant);
                membership.setRole(role);
                membership.setActive(true);
                tenantMembershipRepository.save(membership);
            }
        }

        private void seedPlatformAdmin(String username, String email, String password, Role platformSuperAdmin) {
            if (password == null || password.isBlank()) {
                log.warn("Platform admin seed is enabled but password is not configured. User was not created.");
                return;
            }
            if (username == null || username.isBlank() || email == null || email.isBlank()) {
                log.warn("Platform admin seed is enabled but username or email is not configured. User was not created.");
                return;
            }

            String normalizedUsername = username.trim().toLowerCase();
            String normalizedEmail = email.trim().toLowerCase();
            User user = userRepository.findByUsername(normalizedUsername).orElseGet(User::new);
            boolean emailBelongsToAnotherUser = userRepository.findByEmail(normalizedEmail)
                    .map(existing -> user.getId() == null || !existing.getId().equals(user.getId()))
                    .orElse(false);

            if (emailBelongsToAnotherUser) {
                log.warn("Platform admin seed skipped because configured email is already in use.");
                return;
            }

            if (user.getId() == null) {
                user.setUsername(normalizedUsername);
                user.setFullName("Platform Super Admin");
                user.setEnabled(true);
            }

            user.setEmail(normalizedEmail);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.getRoles().add(platformSuperAdmin);
            userRepository.save(user);
        }
    }
}
