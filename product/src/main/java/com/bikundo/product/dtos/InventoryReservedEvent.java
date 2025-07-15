package com.bikundo.product.dtos;

import lombok.Data;

@Data
public class InventoryReservedEvent {
    private Long orderId;
    private String reservedBy = "INVENTORY-SERVICE";
}
