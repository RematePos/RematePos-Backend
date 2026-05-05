package com.corhuila.microservices.customer_microservice.auth.service.impl;

import com.corhuila.microservices.customer_microservice.auth.dto.AuthResponse;
import com.corhuila.microservices.customer_microservice.auth.dto.LoginRequest;
import com.corhuila.microservices.customer_microservice.auth.dto.RegisterRequest;
import com.corhuila.microservices.customer_microservice.auth.exception.InvalidCredentialsException;
import com.corhuila.microservices.customer_microservice.auth.exception.UserAlreadyExistsException;
import com.corhuila.microservices.customer_microservice.auth.model.AppUser;
import com.corhuila.microservices.customer_microservice.auth.repository.AppUserRepository;
import com.corhuila.microservices.customer_microservice.auth.service.AuthService;
import com.corhuila.microservices.customer_microservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        var user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of("ROLE_USER"))
                .build();

        var savedUser = userRepository.save(user);
        var token = jwtService.generateToken(savedUser.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .username(savedUser.getUsername())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        var token = jwtService.generateToken(request.username());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .username(request.username())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        var username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username)
                .map(user -> jwtService.isTokenValid(token, user.getUsername()))
                .orElse(false);
    }
}
