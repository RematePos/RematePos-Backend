package com.corhuila.microservices.product_microservice.category.service;

import com.corhuila.microservices.product_microservice.category.dto.CategoryOptionResponse;
import com.corhuila.microservices.product_microservice.category.dto.CategoryRequest;
import com.corhuila.microservices.product_microservice.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    Integer createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Integer id);
    List<CategoryResponse> getAllCategories();
    List<CategoryOptionResponse> getCategoryOptions();
    Integer getCategoryIdByName(String name);
    public void deleteCategory(Integer id);
    public Integer updateCategory(CategoryRequest request);





}
