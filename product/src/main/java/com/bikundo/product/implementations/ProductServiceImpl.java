package com.bikundo.product.implementations;

import com.bikundo.product.dtos.CreateProductRequest;
import com.bikundo.product.dtos.ProductDto;
import com.bikundo.product.mappers.ProductMapper;
import com.bikundo.product.models.Product;
import com.bikundo.product.repositories.ProductRepository;
import com.bikundo.product.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDto createProduct(CreateProductRequest request) {
        Assert.notNull(request,"the request should not be null");
        Product product = productMapper.toEntity(request);
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @Override
    public ProductDto getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public ProductDto updateProduct(Long id, CreateProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        existing.setName(request.getName());
        existing.setSku(request.getSku());
        existing.setBrand(request.getBrand());
        existing.setCategory(request.getCategory());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStockQuantity(request.getStockQuantity());

        return productMapper.toDto(productRepository.save(existing));
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }


}
