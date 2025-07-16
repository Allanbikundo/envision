package com.bikundo.order.services;

import java.util.List;
import java.util.UUID;

import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.models.Order.OrderStatus;

public interface OrderService {
    OrderDto placeOrder(CreateOrderRequest request, UUID userId);
    OrderDto getOrderById(Long id);
    void cancelOrder(Long id, UUID userId);
    void updateOrderStatus(Long id, OrderStatus status, String changedBy);
}

