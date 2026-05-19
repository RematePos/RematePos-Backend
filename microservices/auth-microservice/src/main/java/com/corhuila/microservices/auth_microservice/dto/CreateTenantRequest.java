package com.corhuila.microservices.auth_microservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateTenantRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,
        @Valid @NotNull CreateTenantOwnerRequest owner
) {
}
