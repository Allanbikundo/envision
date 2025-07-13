package com.bikundo.order.implementations;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderPlacedEvent {
    private Long orderId;
    private UUID userId;
    private String orderNumber;
    private Long shippingAddressId;
    private Long billingAddressId;
    private List<Item> items;
    private String externalReference;

    @Data
    public static class Item {
        private Long productId;
        private String productSku;
        private Integer quantity;
    }
}

