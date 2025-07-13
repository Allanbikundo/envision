package com.bikundo.order.dtos;

import lombok.Data;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request item to include in a new order")
public class CreateOrderItemRequest {

    @Schema(description = "Product ID", example = "1001", required = true)
    private Long productId;

    @Schema(description = "Product SKU", example = "SKU-1001", required = true)
    private String productSku;

    @Schema(description = "Product name", example = "Samsung A15", required = true)
    private String productName;

    @Schema(description = "Quantity to order", example = "2", required = true)
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "22999.99", required = true)
    private BigDecimal unitPrice;
}

