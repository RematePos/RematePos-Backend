package com.corhuila.microservices.product_microservice.category.service.impl;

import com.corhuila.microservices.product_microservice.category.dto.CategoryRequest;
import com.corhuila.microservices.product_microservice.category.dto.CategoryResponse;
import com.corhuila.microservices.product_microservice.category.dto.CategoryOptionResponse;
import com.corhuila.microservices.product_microservice.category.mapper.CategoryMapper;
import com.corhuila.microservices.product_microservice.category.model.Category;
import com.corhuila.microservices.product_microservice.category.repository.CategoryRepository;
import com.corhuila.microservices.product_microservice.category.service.CategoryService;
import com.corhuila.microservices.product_microservice.exceptions.CategoryException;
import com.corhuila.microservices.product_microservice.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;
    private final SecurityContextHelper security;

    @Override
    public List<CategoryResponse> getAllCategories() {
        String tenantId = security.getTenantId();
        return repository.findAllByTenantId(tenantId).stream()
                .map(mapper::toCategorySummaryResponse)
                .toList();
    }

    @Override
    public Page<CategoryResponse> getCategoriesPage(int page, int size, String search) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by("name").ascending());
        String tenantId = security.getTenantId();

        Page<Category> result = StringUtils.hasText(search)
                ? repository.findByNameContainingIgnoreCaseAndTenantId(search.trim(), tenantId, pageable)
                : repository.findAllByTenantId(tenantId, pageable);

        return result.map(mapper::toCategorySummaryResponse);
    }

    @Override
    public List<CategoryOptionResponse> getCategoryOptions() {
        String tenantId = security.getTenantId();
        return repository.findAllByTenantId(tenantId).stream()
                .map(category -> new CategoryOptionResponse(category.getId(), category.getName()))
                .toList();
    }

    @Override
    public Integer getCategoryIdByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new CategoryException("Category name cannot be null or blank");
        }
        String tenantId = security.getTenantId();
        return repository.findByNameIgnoreCaseAndTenantId(name.trim(), tenantId)
                .map(Category::getId)
                .orElseThrow(() -> new CategoryException("Category with name %s not found".formatted(name)));
    }

    @Override
    public Integer createCategory(CategoryRequest request) {
        String tenantId = security.getTenantId();
        if (repository.existsByNameIgnoreCaseAndTenantId(request.name().trim(), tenantId)) {
            throw new CategoryException("Category with name %s already exists".formatted(request.name()));
        }
        Category category = mapper.toCategory(request);
        category.setTenantId(tenantId);
        return repository.save(category).getId();
    }
    @Override
    public void deleteCategory(Integer id) {
        if (id == null ) {
            throw new CategoryException("Category ID cannot be null or blank");
        }
        String tenantId = security.getTenantId();
        Category category = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new CategoryException("Category with id %s not found".formatted(id)));
        repository.delete(category);
    }
    @Override
    public Integer updateCategory(CategoryRequest request) {
        if (request.id() == null) {
            throw new CategoryException("Category ID cannot be null");
        }
        String tenantId = security.getTenantId();
        Category category = repository.findByIdAndTenantId(request.id(), tenantId)
                .orElseThrow(() -> new CategoryException("Category with ID %s not found".formatted(request.id())));

        if (repository.existsByNameIgnoreCaseAndTenantIdAndIdNot(request.name().trim(), tenantId, request.id())) {
            throw new CategoryException("Category with name %s already exists".formatted(request.name()));
        }

        category.setName(request.name());
        category.setDescription(request.description());
        repository.save(category);
        return category.getId();
    }
    @Override
    public CategoryResponse getCategoryById(Integer id) {
        String tenantId = security.getTenantId();
        return repository
                .findByIdAndTenantId(id, tenantId)
                .map(mapper::toCategoryResponse)
                .orElseThrow(() -> new CategoryException("Category with id %s not found".formatted(id)));
    }

}
