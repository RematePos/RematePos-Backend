package com.corhuila.microservices.product_microservice.category.dto;

import com.corhuila.microservices.product_microservice.product.dto.ProductResponse;

import java.util.List;

public record CategoryResponse(
        Integer id,
        String name,
        String description,
        List<ProductResponse> products
) {

}
