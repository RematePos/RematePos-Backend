package com.corhuila.microservices.product_microservice.product.service.impl;
import java.util.List;

import com.corhuila.microservices.product_microservice.product.dto.ProductQuantityRequest;
import com.corhuila.microservices.product_microservice.product.model.Product;
import org.springframework.stereotype.Service;


import com.corhuila.microservices.product_microservice.exceptions.ProductException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


import com.corhuila.microservices.product_microservice.category.service.CategoryService;

import com.corhuila.microservices.product_microservice.product.dto.ProductRequest;
import com.corhuila.microservices.product_microservice.product.dto.ProductResponse;
import com.corhuila.microservices.product_microservice.product.mapper.ProductMapper;
import com.corhuila.microservices.product_microservice.product.repository.ProductRepository;
import com.corhuila.microservices.product_microservice.product.service.ProductService;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final CategoryService categoryService;
    private final ProductMapper mapper;

    public List<ProductResponse> getProducts() {
        return repository.findAll().stream()
                .map(mapper::toProductResponse)
                .toList();
    }

    public ProductResponse getProductById(Integer id) {
        if (id == null) {
            throw new ProductException("Product ID cannot be null");
        }
        return repository.findById(id)
                .map(mapper::toProductResponse)
                .orElse(null);
    }

    public List<ProductResponse> getProductsByCategoryId(Integer id) {
        if (id == null) {
            throw new ProductException("Category ID cannot be null");
        }
        return repository.findAll().stream()
                .filter(product -> product.getCategory().getId().equals(id))
                .map(mapper::toProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getProductsByCategoryName(String name) {
        Integer categoryId = categoryService.getCategoryIdByName(name);
        return getProductsByCategoryId(categoryId);
    }


    public Integer updateProduct(ProductRequest request) {
        if (request.id() == null) {
            throw new ProductException("Product ID cannot be null");
        }

        if (categoryService.getCategoryById(request.categoryId()) == null) {
            throw new ProductException("Category with ID %s not found".formatted(request.categoryId()));
        }

        Product existingProduct = repository.findById(request.id())
                .orElseThrow(() -> new ProductException("Product with ID %s not found".formatted(request.id())));

        Product updatedProduct = mapper.toProduct(request);

        // Ensure we do not overwrite stock
        updatedProduct.setStock(existingProduct.getStock());

        repository.save(updatedProduct);
        return updatedProduct.getId();
    }

    public void deleteProduct(Integer id) {
        if (id == null) {
            throw new ProductException("Product ID cannot be null");
        }
        if (!repository.existsById(id)) {
            throw new ProductException("Product with ID %s not found".formatted(id));
        }
        repository.deleteById(id);
    }

    public Integer createProduct(ProductRequest product) {

        if (categoryService.getCategoryById(product.categoryId()) == null) {
            throw new ProductException("Category with ID %s not found".formatted(product.categoryId()));
        }

        Product newProduct = mapper.toProduct(product);
        Product savedProduct = repository.save(newProduct);
        return savedProduct.getId();
    }

    @Transactional
    public void purchaseProduct(List<ProductQuantityRequest> request) {
        for (ProductQuantityRequest item : request) {

            Product product = repository.findById(item.productId())
                    .orElseThrow(() -> new ProductException("Product with ID %s not found".formatted(item.productId())));

            if (item.quantity() < 0) {
                throw new ProductException("Restock quantity cannot be negative for product ID %s".formatted(item.productId()));
            }

            if (product.getStock() < item.quantity()) {
                throw new ProductException("Insufficient stock for product ID %s".formatted(item.productId()));
            }

            product.setStock(product.getStock() - item.quantity());
            repository.save(product);
        }
    }

    @Transactional
    public void restockProduct(List<ProductQuantityRequest> request) {
        for (ProductQuantityRequest item : request) {
            Product product = repository.findById(item.productId())
                    .orElseThrow(() -> new ProductException("Product with ID %s not found".formatted(item.productId())));

            if (item.quantity() < 0) {
                throw new ProductException("Restock quantity cannot be negative for product ID %s".formatted(item.productId()));
            }

            product.setStock(product.getStock() + item.quantity());
            repository.save(product);
        }
    }

}