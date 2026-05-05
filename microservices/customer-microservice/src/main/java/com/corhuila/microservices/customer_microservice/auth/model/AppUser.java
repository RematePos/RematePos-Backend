package com.corhuila.microservices.customer_microservice.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "users")
public class AppUser {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private Set<String> roles;
}
