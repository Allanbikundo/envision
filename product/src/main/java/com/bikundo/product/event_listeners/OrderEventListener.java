package com.bikundo.product.event_listeners;

import com.bikundo.product.config.RabbitMQConfig;
import com.bikundo.product.dtos.InventoryFailedEvent;
import com.bikundo.product.dtos.InventoryReservedEvent;
import com.bikundo.product.dtos.OrderPlacedEvent;
import com.bikundo.product.models.Product;
import com.bikundo.product.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {



    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "${mq.order.placed.queue:order.placed.queue}")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Processing order placed: {}", event.getOrderNumber());

        try {
            for (OrderPlacedEvent.Item item : event.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));

                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for SKU: " + product.getSku());
                }

                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }

            // ✅ Success → publish InventoryReservedEvent
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent();
            reservedEvent.setOrderId(event.getOrderId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
                    reservedEvent
            );

        } catch (Exception ex) {
            log.error("Inventory reservation failed for order {}: {}", event.getOrderId(), ex.getMessage());

            // ❌ Failure → publish InventoryFailedEvent
            InventoryFailedEvent failedEvent = new InventoryFailedEvent();
            failedEvent.setOrderId(event.getOrderId());
            failedEvent.setReason(ex.getMessage());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_FAILED_ROUTING_KEY,
                    failedEvent
            );
        }
    }
}
