package com.bikundo.product.dtos;

import lombok.Data;

@Data
public class OrderResult {
    private Long orderId;
    private OrderStatus orderStatus;
    private String message;
    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED, FAILED
    }
}
