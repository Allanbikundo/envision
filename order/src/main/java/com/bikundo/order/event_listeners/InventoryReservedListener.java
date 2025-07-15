package com.bikundo.order.event_listeners;

import com.bikundo.order.dtos.InventoryReservedEvent;
import com.bikundo.order.models.Order;
import com.bikundo.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservedListener {

    private final OrderService orderService;

    @RabbitListener(queues = "${mq.inventory.reserved.queue:inventory.reserved.queue}")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order {}", event.getOrderId());

        try {
            orderService.updateOrderStatus(event.getOrderId(), Order.OrderStatus.CONFIRMED, event.getReservedBy());
        } catch (Exception ex) {
            log.error("Failed to mark order {} as CONFIRMED", event.getOrderId(), ex);
        }
    }
}
