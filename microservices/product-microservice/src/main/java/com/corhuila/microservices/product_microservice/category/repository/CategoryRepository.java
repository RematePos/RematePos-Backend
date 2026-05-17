package com.corhuila.microservices.product_microservice.category.repository;

import com.corhuila.microservices.product_microservice.category.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer> {
	Optional<Category> findByNameIgnoreCase(String name);
	Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
	List<Category> findAllByTenantId(String tenantId);
	Page<Category> findAllByTenantId(String tenantId, Pageable pageable);
	Optional<Category> findByIdAndTenantId(Integer id, String tenantId);
	Optional<Category> findByNameIgnoreCaseAndTenantId(String name, String tenantId);
	Page<Category> findByNameContainingIgnoreCaseAndTenantId(String name, String tenantId, Pageable pageable);
	boolean existsByIdAndTenantId(Integer id, String tenantId);
	boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);
	boolean existsByNameIgnoreCaseAndTenantIdAndIdNot(String name, String tenantId, Integer id);
}
