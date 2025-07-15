package com.bikundo.product.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String sku;
    private String brand;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Instant createdAt;
    private Instant updatedAt;
}
