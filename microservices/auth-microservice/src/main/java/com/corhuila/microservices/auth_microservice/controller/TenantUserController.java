package com.corhuila.microservices.auth_microservice.controller;

import com.corhuila.microservices.auth_microservice.dto.CreateTenantUserRequest;
import com.corhuila.microservices.auth_microservice.dto.TenantUserResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantUserStatusResponse;
import com.corhuila.microservices.auth_microservice.dto.UpdateTenantUserRequest;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.service.TenantUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
public class TenantUserController {

    private final TenantUserService tenantUserService;

    public TenantUserController(TenantUserService tenantUserService) {
        this.tenantUserService = tenantUserService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TenantUserResponse> createTenantUser(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateTenantUserRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantUserService.createTenantUser(tenantId, request, currentUser));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TenantUserResponse> listTenantUsers(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal User currentUser
    ) {
        return tenantUserService.listTenantUsers(tenantId, currentUser);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public TenantUserResponse updateTenantUser(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateTenantUserRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return tenantUserService.updateTenantUser(tenantId, userId, request, currentUser);
    }

    @PatchMapping("/{userId}/disable")
    @PreAuthorize("isAuthenticated()")
    public TenantUserStatusResponse disableTenantUser(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        return tenantUserService.disableTenantUser(tenantId, userId, currentUser);
    }

    @PatchMapping("/{userId}/enable")
    @PreAuthorize("isAuthenticated()")
    public TenantUserStatusResponse enableTenantUser(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        return tenantUserService.enableTenantUser(tenantId, userId, currentUser);
    }
}
