package com.corhuila.microservices.invoice_microservice.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";
    private static final String INTERNAL_TOKEN_PLACEHOLDER = "change_me_use_secret_store";

    private final HttpServletRequest request;

    @Value("${internal.service.token:}")
    private String internalServiceToken;

    public CurrentUserContext requireAuthenticated() {
        var context = currentUserContext();
        if (isBlank(context.userId()) || isBlank(context.username())) {
            throw new InvoiceSecurityException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return context;
    }

    public CurrentUserContext requireTenant() {
        var context = requireAuthenticated();
        if (isBlank(context.tenantId())) {
            throw new InvoiceSecurityException(HttpStatus.FORBIDDEN, "Tenant context is required");
        }
        return context;
    }

    public String getTenantId() {
        return requireTenant().tenantId();
    }

    public String getTenantIdForInternalService(String serviceName) {
        if (isInternalServiceRequest(serviceName)) {
            var context = currentUserContext();
            if (isBlank(context.tenantId())) {
                throw new InvoiceSecurityException(HttpStatus.FORBIDDEN, "Tenant context is required");
            }
            return context.tenantId();
        }
        return getTenantId();
    }

    private boolean isInternalServiceRequest(String serviceName) {
        String incomingService = request.getHeader(INTERNAL_SERVICE_HEADER);
        String incomingToken = request.getHeader(INTERNAL_SERVICE_TOKEN_HEADER);
        if (!serviceName.equals(incomingService)) {
            return false;
        }
        if (isBlank(internalServiceToken) || INTERNAL_TOKEN_PLACEHOLDER.equals(internalServiceToken)) {
            throw new InvoiceSecurityException(HttpStatus.UNAUTHORIZED, "Internal service authentication is not configured");
        }
        if (!internalServiceToken.equals(incomingToken)) {
            throw new InvoiceSecurityException(HttpStatus.UNAUTHORIZED, "Invalid internal service authentication");
        }
        return true;
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
