package com.corhuila.microservices.auth_microservice.controller;

import com.corhuila.microservices.auth_microservice.dto.CreateTenantRequest;
import com.corhuila.microservices.auth_microservice.dto.CreateTenantResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantResponse;
import com.corhuila.microservices.auth_microservice.dto.TenantStatusUpdateResponse;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.service.TenantService;
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
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN') or hasAuthority('TENANTS_CREATE')")
    public ResponseEntity<CreateTenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenantWithOwner(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TenantResponse> listTenants(@AuthenticationPrincipal User currentUser) {
        return tenantService.listTenantsForCurrentUser(currentUser);
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("isAuthenticated()")
    public TenantResponse getTenant(@PathVariable Long tenantId, @AuthenticationPrincipal User currentUser) {
        return tenantService.getTenantById(tenantId, currentUser);
    }

    @PatchMapping("/{tenantId}/suspend")
    @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN') or hasAuthority('TENANTS_SUSPEND')")
    public TenantStatusUpdateResponse suspendTenant(@PathVariable Long tenantId) {
        return tenantService.suspendTenant(tenantId);
    }

    @PatchMapping("/{tenantId}/activate")
    @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN') or hasAuthority('TENANTS_SUSPEND')")
    public TenantStatusUpdateResponse activateTenant(@PathVariable Long tenantId) {
        return tenantService.activateTenant(tenantId);
    }
}
