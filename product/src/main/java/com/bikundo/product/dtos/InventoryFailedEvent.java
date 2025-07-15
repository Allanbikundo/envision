package com.bikundo.product.dtos;

import lombok.Data;

@Data
public class InventoryFailedEvent {
    private Long orderId;
    private String reason;
}
