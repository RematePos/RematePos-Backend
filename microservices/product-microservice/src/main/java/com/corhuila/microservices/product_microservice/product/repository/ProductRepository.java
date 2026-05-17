package com.corhuila.microservices.product_microservice.product.repository;

import com.corhuila.microservices.product_microservice.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findAllByTenantId(String tenantId);

    List<Product> findAllByCategoryIdAndTenantId(Integer categoryId, String tenantId);

    Optional<Product> findByIdAndTenantId(Integer id, String tenantId);

    boolean existsByIdAndTenantId(Integer id, String tenantId);

}
