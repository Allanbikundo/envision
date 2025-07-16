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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {



    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "${mq.order.placed.queue:order.placed.queue}")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Processing order placed: {}", event.getOrderId());

        try {
            // Step 1: Fetch all products involved
            List<Long> productIds = event.getItems().stream()
                    .map(OrderPlacedEvent.Item::getProductId)
                    .toList();

            Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            // Step 2: Validate stock availability
            for (OrderPlacedEvent.Item item : event.getItems()) {
                Product product = productMap.get(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }

                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for SKU: " + product.getSku());
                }
            }

            // Step 3: All good → reduce stock
            for (OrderPlacedEvent.Item item : event.getItems()) {
                Product product = productMap.get(item.getProductId());
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            }

            productRepository.saveAll(productMap.values());

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
