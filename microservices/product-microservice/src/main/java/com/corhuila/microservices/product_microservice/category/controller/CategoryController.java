package com.corhuila.microservices.product_microservice.category.controller;

import java.util.List;

import com.corhuila.microservices.product_microservice.category.dto.CategoryRequest;
import com.corhuila.microservices.product_microservice.category.dto.CategoryResponse;
import com.corhuila.microservices.product_microservice.category.dto.CategoryOptionResponse;
import com.corhuila.microservices.product_microservice.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping()
    public ResponseEntity<?> getAllCategories(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "search", required = false) String search
    ) {
        if (page != null || size != null || search != null) {
            Page<CategoryResponse> result = service.getCategoriesPage(
                    page == null ? 0 : page,
                    size == null ? 5 : size,
                    search
            );
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(service.getAllCategories());
    }

    @GetMapping("/options")
    public ResponseEntity<List<CategoryOptionResponse>> getCategoryOptions() {
        return ResponseEntity.ok(service.getCategoryOptions());
    }

    @PostMapping()
    public ResponseEntity<Integer> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(service.createCategory(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Integer id) throws Exception {

        service.deleteCategory(id);
        return ResponseEntity.accepted().build();
    }

    @PutMapping()
    public ResponseEntity<Void> updateCategory(@Valid @RequestBody CategoryRequest request) {

        service.updateCategory(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(service.getCategoryById(id));
    }
}
