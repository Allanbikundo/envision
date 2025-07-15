package com.bikundo.product.dtos;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderPlacedEvent {
    private Long orderId;
    private UUID userId;
    private String orderNumber;
    private Long shippingAddressId;
    private Long billingAddressId;
    private String externalReference;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private String productSku;
        private Integer quantity;
    }
}
