package com.bikundo.order.dtos;

import com.bikundo.order.models.Order;
import lombok.Data;

@Data
public class OrderResult {
    private Long orderId;
    private Order.OrderStatus orderStatus;
}
