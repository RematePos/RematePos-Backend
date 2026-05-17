package com.corhuila.microservices.purchase_microservice.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";
    private static final String ROLES_HEADER = "X-Roles";
    private static final String PERMISSIONS_HEADER = "X-Permissions";
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String TENANT_SLUG_HEADER = "X-Tenant-Slug";

    private final HttpServletRequest request;

    public CurrentUserContext requireAuthenticated() {
        var context = currentUserContext();
        if (isBlank(context.userId()) || isBlank(context.username())) {
            throw new PurchaseSecurityException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return context;
    }

    public CurrentUserContext requireTenant() {
        var context = requireAuthenticated();
        if (isBlank(context.tenantId())) {
            throw new PurchaseSecurityException(HttpStatus.FORBIDDEN, "Tenant context is required");
        }
        return context;
    }

    public CurrentUserContext requirePermission(String permission) {
        var context = requireTenant();
        if (!context.permissions().contains(permission)) {
            throw new PurchaseSecurityException(HttpStatus.FORBIDDEN, "Required permission is missing");
        }
        return context;
    }

    public String getTenantId() {
        return requireTenant().tenantId();
    }

    private CurrentUserContext currentUserContext() {
        return new CurrentUserContext(
                request.getHeader(USER_ID_HEADER),
                request.getHeader(USERNAME_HEADER),
                parseHeaderValues(request.getHeader(ROLES_HEADER)),
                parseHeaderValues(request.getHeader(PERMISSIONS_HEADER)),
                request.getHeader(TENANT_ID_HEADER),
                request.getHeader(TENANT_SLUG_HEADER)
        );
    }

    private Set<String> parseHeaderValues(String value) {
        if (isBlank(value)) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
