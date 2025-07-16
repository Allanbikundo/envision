package com.bikundo.order.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for placing a new order")
@Data
public class CreateOrderRequest {
    @Schema(example = "MPESA-12345", description = "External reference for the order")
    @Size(max = 100, message = "External reference must not exceed 100 characters")
    private String externalReference;

    @Schema(example = "john@example.com", description = "Customer contact email")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @Schema(example = "+254712345678", description = "Customer contact phone number")
    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
    private String contactPhone;

    @Schema(description = "Optional notes or delivery instructions")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<CreateOrderItemRequest> items;
}

