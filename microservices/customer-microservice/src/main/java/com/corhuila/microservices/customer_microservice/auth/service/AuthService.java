package com.corhuila.microservices.customer_microservice.auth.service;

import com.corhuila.microservices.customer_microservice.auth.dto.AuthResponse;
import com.corhuila.microservices.customer_microservice.auth.dto.LoginRequest;
import com.corhuila.microservices.customer_microservice.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    boolean validateToken(String token);
}
