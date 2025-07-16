package com.bikundo.order.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request item to include in a new order")
public class CreateOrderItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", example = "1001", required = true)
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to order", example = "2", required = true)
    private Integer quantity;
}

