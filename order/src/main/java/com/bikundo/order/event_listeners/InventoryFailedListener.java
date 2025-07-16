package com.bikundo.order.event_listeners;

import com.bikundo.order.dtos.InventoryFailedEvent;
import com.bikundo.order.models.Order;
import com.bikundo.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryFailedListener {

    private final OrderService orderService;

    @RabbitListener(queues = "${mq.inventory.failed.queue:inventory.failed.queue}")
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.warn("Inventory failed for order {}: {}", event.getOrderId(), event.getReason());

        try {
            orderService.updateOrderStatus(event.getOrderId(), Order.OrderStatus.FAILED, "INVENTORY-SERVICE");
        } catch (Exception ex) {
            log.error("Failed to mark order {} as FAILED", event.getOrderId(), ex);
        }
    }
}
