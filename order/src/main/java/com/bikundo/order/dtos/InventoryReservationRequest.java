package com.bikundo.order.dtos;

import lombok.Data;
import java.util.UUID;

@Data
public class InventoryReservationRequest {
    private Long productId;
    private String productSku;
    private Integer quantity;
    private UUID orderId;
    private String reason;
}
