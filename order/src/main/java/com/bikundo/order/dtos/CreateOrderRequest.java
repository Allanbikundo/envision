package com.bikundo.order.dtos;

import lombok.Data;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for placing a new order")
@Data
public class CreateOrderRequest {
    @Schema(example = "MPESA-12345", description = "External reference for the order")
    private String externalReference;

    @Schema(example = "john@example.com")
    private String contactEmail;

    @Schema(example = "+254712345678")
    private String contactPhone;

    private Long shippingAddressId;
    private Long billingAddressId;

    private String notes;
    private List<CreateOrderItemRequest> items;
}

