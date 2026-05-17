package com.corhuila.gateway.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtGatewayFilter extends OncePerRequestFilter {

    private static final List<String> DEFAULT_PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/actuator/health"
    );
    private static final List<String> INTERNAL_HEADER_NAMES = List.of(
            "X-User-Id",
            "X-Username",
            "X-Roles",
            "X-Permissions"
    );

    private final JwtValidationService jwtValidationService;
    private final List<String> publicPaths;

    public JwtGatewayFilter(
            JwtValidationService jwtValidationService,
            @Value("${gateway.public-paths:}") String publicPathsProperty
    ) {
        this.jwtValidationService = jwtValidationService;
        this.publicPaths = parsePublicPaths(publicPathsProperty);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!request.getRequestURI().startsWith("/api/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            rejectUnauthorized(response);
            return;
        }

        try {
            JwtClaims claims = jwtValidationService.validate(authorization.substring(7));
            filterChain.doFilter(new InternalHeadersRequestWrapper(request, claims), response);
        } catch (JwtException | IllegalArgumentException exception) {
            rejectUnauthorized(response);
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        return publicPaths.stream().anyMatch(uri::equals);
    }

    private List<String> parsePublicPaths(String publicPathsProperty) {
        if (publicPathsProperty == null || publicPathsProperty.isBlank()) {
            return DEFAULT_PUBLIC_PATHS;
        }

        return java.util.stream.Stream.concat(
                        DEFAULT_PUBLIC_PATHS.stream(),
                        java.util.Arrays.stream(publicPathsProperty.split(","))
                                .map(String::trim)
                                .filter(path -> !path.isBlank())
                )
                .distinct()
                .toList();
    }

    private void rejectUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Unauthorized\"}");
    }

    private static class InternalHeadersRequestWrapper extends HttpServletRequestWrapper {

        private final JwtClaims claims;

        InternalHeadersRequestWrapper(HttpServletRequest request, JwtClaims claims) {
            super(request);
            this.claims = claims;
        }

        @Override
        public String getHeader(String name) {
            if (isInternalHeader(name)) return internalHeaderValue(name);
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isInternalHeader(name)) {
                return Collections.enumeration(List.of(internalHeaderValue(name)));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = new ArrayList<>();
            Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                String name = originalNames.nextElement();
                if (!isInternalHeader(name)) {
                    names.add(name);
                }
            }
            names.addAll(INTERNAL_HEADER_NAMES);
            return Collections.enumeration(names);
        }

        private boolean isInternalHeader(String name) {
            return name != null && INTERNAL_HEADER_NAMES.stream().anyMatch(header -> header.equalsIgnoreCase(name));
        }

        private String internalHeaderValue(String name) {
            if ("X-User-Id".equalsIgnoreCase(name)) return claims.userId();
            if ("X-Username".equalsIgnoreCase(name)) return claims.username();
            if ("X-Roles".equalsIgnoreCase(name)) return String.join(",", claims.roles());
            if ("X-Permissions".equalsIgnoreCase(name)) return String.join(",", claims.permissions());
            throw new IllegalArgumentException("Unsupported internal header");
        }
    }
}
