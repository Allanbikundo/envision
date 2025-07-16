package com.bikundo.order.dtos;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderPlacedEvent implements Serializable {
    private Long orderId;
    private UUID userId;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}

