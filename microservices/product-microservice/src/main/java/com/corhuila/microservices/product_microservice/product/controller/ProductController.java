package com.corhuila.microservices.product_microservice.product.controller;
import java.util.List;

import com.corhuila.microservices.product_microservice.product.dto.ProductQuantityRequest;
import com.corhuila.microservices.product_microservice.product.dto.ProductRequest;
import com.corhuila.microservices.product_microservice.product.dto.ProductResponse;
import com.corhuila.microservices.product_microservice.product.service.ProductService;
import com.corhuila.microservices.product_microservice.security.SecurityContextHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final SecurityContextHelper security;

    @PostMapping()
    public ResponseEntity<Integer> createProduct(@Valid @RequestBody ProductRequest product) {
        security.requirePermission("PRODUCTS_CREATE");
        return ResponseEntity.ok(service.createProduct(product));
    }

    @GetMapping()
    public List<ProductResponse> getProducts() {
        security.requirePermission("PRODUCTS_READ");
        return service.getProducts();
    }

    @GetMapping("/category/{id}")
    public List<ProductResponse> getProductsByCategoryId(@PathVariable Integer id) {
        security.requirePermission("PRODUCTS_READ");
        return service.getProductsByCategoryId(id);
    }

    @GetMapping("/category/name/{name}")
    public List<ProductResponse> getProductsByCategoryName(@PathVariable String name) {
        security.requirePermission("PRODUCTS_READ");
        return service.getProductsByCategoryName(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        security.requirePermission("PRODUCTS_READ");
        return ResponseEntity.ok(service.getProductById(id));
    }

    @PutMapping()
    public ResponseEntity<Integer> updateProduct(@Valid @RequestBody ProductRequest product) {
        security.requirePermission("PRODUCTS_UPDATE");
        return ResponseEntity.ok(service.updateProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Integer> updateProductById(
            @PathVariable Integer id,
            @Valid @RequestBody ProductRequest product
    ) {
        security.requirePermission("PRODUCTS_UPDATE");
        ProductRequest request = new ProductRequest(
                id,
                product.name(),
                product.description(),
                product.price(),
                product.stock(),
                product.imageUrl(),
                product.categoryId()
        );
        return ResponseEntity.ok(service.updateProduct(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        security.requirePermission("PRODUCTS_DELETE");
        service.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase")
    public ResponseEntity<Void> purchaseProduct(@Valid @RequestBody List<ProductQuantityRequest> request) {
        security.requirePermissionOrInternalService("SALES_CREATE", "purchase-microservice");
        service.purchaseProduct(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restock")
    public ResponseEntity<Void> updateProductStock(@Valid @RequestBody List<ProductQuantityRequest> request) {
        security.requirePermissionOrInternalService("PRODUCTS_UPDATE", "purchase-microservice");
        service.restockProduct(request);
        return ResponseEntity.ok().build();
    }

}
