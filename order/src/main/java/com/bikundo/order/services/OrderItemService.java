package com.bikundo.order.services;

import java.util.List;

import com.bikundo.order.dtos.CreateOrderItemRequest;
import com.bikundo.order.dtos.OrderItemDto;

public interface OrderItemService {
    List<OrderItemDto> getItemsByOrderId(Long orderId);
    void addItemsToOrder(Long orderId, List<CreateOrderItemRequest> items);
}

