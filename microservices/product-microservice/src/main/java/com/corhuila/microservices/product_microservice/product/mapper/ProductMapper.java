package com.corhuila.microservices.product_microservice.product.mapper;

import com.corhuila.microservices.product_microservice.category.model.Category;
import com.corhuila.microservices.product_microservice.product.dto.ProductRequest;
import com.corhuila.microservices.product_microservice.product.dto.ProductResponse;
import com.corhuila.microservices.product_microservice.product.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getDescription()
        );
    }

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .category(Category.builder()
                        .id(request.categoryId())
                        .build())
                .build();
    }
}