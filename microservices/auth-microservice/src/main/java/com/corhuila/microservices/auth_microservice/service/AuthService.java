package com.corhuila.microservices.auth_microservice.service;

import com.corhuila.microservices.auth_microservice.dto.LoginRequest;
import com.corhuila.microservices.auth_microservice.dto.LoginResponse;
import com.corhuila.microservices.auth_microservice.dto.RegisterRequest;
import com.corhuila.microservices.auth_microservice.dto.UserSessionResponse;
import com.corhuila.microservices.auth_microservice.model.User;
import com.corhuila.microservices.auth_microservice.repository.UserRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final boolean demoRegistrationEnabled;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            UserService userService,
            @Value("${auth.demo-registration-enabled:false}") boolean demoRegistrationEnabled
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userService = userService;
        this.demoRegistrationEnabled = demoRegistrationEnabled;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationMs(), userService.toSessionResponse(user));
    }

    @Transactional(readOnly = true)
    public UserSessionResponse currentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        return userService.toSessionResponse(user);
    }

    @Transactional
    public UserSessionResponse registerDemoUser(RegisterRequest request) {
        if (!demoRegistrationEnabled) {
            throw new IllegalStateException("El registro demo esta deshabilitado");
        }
        User user = userService.createUser(request, Set.of("CASHIER", "QA_SUPPORT"));
        return userService.toSessionResponse(user);
    }
}
