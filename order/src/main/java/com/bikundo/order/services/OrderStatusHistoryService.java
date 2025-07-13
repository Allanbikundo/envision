package com.bikundo.order.services;

import java.util.List;
import java.util.UUID;

import com.bikundo.order.dtos.OrderStatusHistoryDto;
import com.bikundo.order.models.Order.OrderStatus;

public interface OrderStatusHistoryService {
    void recordStatusChange(Long orderId, OrderStatus previous, OrderStatus current, UUID changedBy, String reason);
    List<OrderStatusHistoryDto> getHistoryForOrder(Long orderId);
}

