package com.corhuila.microservices.product_microservice.product.repository;

import com.corhuila.microservices.product_microservice.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {


}