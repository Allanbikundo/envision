package com.bikundo.product.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    private String name;
    private String sku;
    private String brand;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
}
