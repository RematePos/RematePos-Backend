package com.corhuila.microservices.product_microservice.category.service.impl;

import com.corhuila.microservices.product_microservice.category.dto.CategoryRequest;
import com.corhuila.microservices.product_microservice.category.dto.CategoryResponse;
import com.corhuila.microservices.product_microservice.category.dto.CategoryOptionResponse;
import com.corhuila.microservices.product_microservice.category.mapper.CategoryMapper;
import com.corhuila.microservices.product_microservice.category.model.Category;
import com.corhuila.microservices.product_microservice.category.repository.CategoryRepository;
import com.corhuila.microservices.product_microservice.category.service.CategoryService;
import com.corhuila.microservices.product_microservice.exceptions.CategoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return repository.findAll().stream()
                .map(mapper::toCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryOptionResponse> getCategoryOptions() {
        return repository.findAll().stream()
                .map(category -> new CategoryOptionResponse(category.getId(), category.getName()))
                .toList();
    }

    @Override
    public Integer getCategoryIdByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new CategoryException("Category name cannot be null or blank");
        }
        return repository.findByNameIgnoreCase(name.trim())
                .map(Category::getId)
                .orElseThrow(() -> new CategoryException("Category with name %s not found".formatted(name)));
    }

    @Override
    public Integer createCategory(CategoryRequest request) {
        Category category = mapper.toCategory(request);
        return repository.save(category).getId();
    }
    @Override
    public void deleteCategory(Integer id) {
        if (id == null ) {
            throw new CategoryException("Category ID cannot be null or blank");
        }
        else if (!repository.existsById(id)) {
            throw new CategoryException("Category with id %s not found".formatted(id));
        }
        repository.deleteById(id);
    }
    @Override
    public Integer updateCategory(CategoryRequest request) {
        Category category = mapper.toCategory(request);
        if (request.id() == null) {
            throw new CategoryException("Category ID cannot be null");
        }
        else if (!repository.existsById(request.id())) {
            throw new CategoryException("Category with ID %s not found".formatted(request.id()));
        }
        repository.save(category);
        return category.getId();
    }
    @Override
    public CategoryResponse getCategoryById(Integer id) {
        return repository
                .findById(id)
                .map(mapper::toCategoryResponse)
                .orElseThrow(() -> new CategoryException("Category with id %s not found".formatted(id)));
    }

}