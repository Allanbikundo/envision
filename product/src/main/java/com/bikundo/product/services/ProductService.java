package com.bikundo.product.services;

import com.bikundo.product.dtos.CreateProductRequest;
import com.bikundo.product.dtos.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto createProduct(CreateProductRequest request);

    ProductDto getProductById(Long id);

    ProductDto getProductBySku(String sku);

    List<ProductDto> getAllProducts();

    ProductDto updateProduct(Long id, CreateProductRequest request);

    void deleteProduct(Long id);
}
