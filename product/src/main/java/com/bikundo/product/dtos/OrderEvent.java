package com.bikundo.product.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class OrderEvent implements Serializable {
    private Long orderId;
    private UUID userId;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}