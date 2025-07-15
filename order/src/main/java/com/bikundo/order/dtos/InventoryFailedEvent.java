package com.bikundo.order.dtos;

import lombok.Data;

@Data
public class InventoryFailedEvent {
    private Long orderId;
    private String reason;
}
