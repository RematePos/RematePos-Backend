package com.corhuila.microservices.product_microservice.category.mapper;

import com.corhuila.microservices.product_microservice.category.dto.CategoryRequest;
import com.corhuila.microservices.product_microservice.category.dto.CategoryResponse;
import com.corhuila.microservices.product_microservice.category.model.Category;
import com.corhuila.microservices.product_microservice.product.dto.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {

    public Category toCategory(CategoryRequest request) {
        return  Category.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getProducts().stream()
                        .map(product -> new ProductResponse(
                                product.getId(),
                                product.getName(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getStock(),
                                product.getImageUrl(),
                                product.getCategory().getId(),
                                product.getCategory().getName(),
                                product.getCategory().getDescription(
                                )))
                        .toList()
        );
    }
}
