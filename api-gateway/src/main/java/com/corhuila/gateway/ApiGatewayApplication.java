package com.corhuila.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import static org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions.removeResponseHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@SpringBootApplication
public class ApiGatewayApplication {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://localhost:13000",
            "http://localhost:33000"
    );

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> gatewayRoutes(
            @Value("${services.customer.url:http://customer-microservice:8091}") String customerUrl,
            @Value("${services.product.url:http://product-microservice:8092}") String productUrl,
            @Value("${services.cart.url:http://cart-microservice:8093}") String cartUrl,
            @Value("${services.purchase.url:http://purchase-microservice:8094}") String purchaseUrl,
            @Value("${services.invoice.url:http://invoice-microservice:8095}") String invoiceUrl
    ) {
        return route("customer-route")
                .route(path("/api/v1/customers/**"), http())
                .before(uri(customerUrl))
                .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .build()
                .and(route("product-route")
                        .route(path("/api/v1/products/**"), http())
                        .before(uri(productUrl))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                        .build())
                .and(route("category-route")
                        .route(path("/api/v1/categories/**"), http())
                        .before(uri(productUrl))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                        .build())
                .and(route("cart-route")
                        .route(path("/api/v1/carts/**"), http())
                        .before(uri(cartUrl))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                        .build())
                .and(route("purchase-route")
                        .route(path("/api/v1/purchases/**"), http())
                        .before(uri(purchaseUrl))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                        .build())
                .and(route("invoice-route")
                        .route(path("/api/v1/invoices/**"), http())
                        .before(uri(invoiceUrl))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                        .after(removeResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                        .build());
    }

    @Bean
    OncePerRequestFilter corsFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                applyCorsHeaders(request, response);

                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

                filterChain.doFilter(request, response);
                applyCorsHeaders(request, response);
            }
        };
    }

    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
            return;
        }

        String requestedHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        response.setHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                requestedHeaders == null || requestedHeaders.isBlank() ? "*" : requestedHeaders
        );
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1800");
    }
}
