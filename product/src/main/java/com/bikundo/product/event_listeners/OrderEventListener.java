package com.bikundo.product.event_listeners;

import com.bikundo.product.config.RabbitMQConfig;
import com.bikundo.product.dtos.OrderPlacedEvent;
import com.bikundo.product.models.Product;
import com.bikundo.product.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductRepository productRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for order {}", event.getOrderNumber());

        try {
            for (OrderPlacedEvent.Item item : event.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));

                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }

                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);

                log.info("Reserved {} units of SKU {} (new stock: {})",
                        item.getQuantity(), product.getSku(), product.getStockQuantity());
            }
        } catch (Exception e) {
            log.error("Failed to process order {}: {}", event.getOrderNumber(), e.getMessage());
            // Don't rethrow - this will acknowledge the message and prevent requeuing
            // Optionally send to dead letter queue or emit a failure event
        }

        // (Optionally emit InventoryReservedEvent here)
    }
}
