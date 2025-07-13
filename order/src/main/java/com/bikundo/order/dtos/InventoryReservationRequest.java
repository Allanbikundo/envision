package com.bikundo.order.dtos;

import lombok.Data;
import java.util.UUID;

@Data
public class InventoryReservationRequest {
    private Long productId;
    private String productSku;
    private Integer quantity;
    private UUID orderId; // Optional if reservation is within same service
    private String reason; // Optional field like 'ORDER_PLACEMENT', 'RETRY'
}
