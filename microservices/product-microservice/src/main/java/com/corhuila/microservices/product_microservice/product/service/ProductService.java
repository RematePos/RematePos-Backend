
package com.corhuila.microservices.product_microservice.product.service;

import com.corhuila.microservices.product_microservice.product.dto.ProductQuantityRequest;
import com.corhuila.microservices.product_microservice.product.dto.ProductRequest;
import com.corhuila.microservices.product_microservice.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse>getProducts();
    ProductResponse getProductById(Integer id);
    List<ProductResponse> getProductsByCategoryId(Integer id);
    Integer updateProduct(ProductRequest request);
    void deleteProduct(Integer id);
    Integer createProduct(ProductRequest request);
    void purchaseProduct(List<ProductQuantityRequest> request);
    void restockProduct(List<ProductQuantityRequest> request);

}
