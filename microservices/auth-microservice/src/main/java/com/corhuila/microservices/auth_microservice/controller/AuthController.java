package com.corhuila.microservices.auth_microservice.controller;

import com.corhuila.microservices.auth_microservice.dto.LoginRequest;
import com.corhuila.microservices.auth_microservice.dto.LoginResponse;
import com.corhuila.microservices.auth_microservice.dto.RegisterRequest;
import com.corhuila.microservices.auth_microservice.dto.UserSessionResponse;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSessionResponse> me(@AuthenticationPrincipal User user, Principal principal) {
        String username = user != null ? user.getUsername() : principal.getName();
        return ResponseEntity.ok(authService.currentUser(username));
    }

    @PostMapping("/register")
    public ResponseEntity<UserSessionResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerDemoUser(request));
    }
}
