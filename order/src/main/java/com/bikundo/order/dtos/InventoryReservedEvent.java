package com.bikundo.order.dtos;

import lombok.Data;

@Data
public class InventoryReservedEvent {
    private Long orderId;
    private String reservedBy = "INVENTORY-SERVICE"; // Optional
}
