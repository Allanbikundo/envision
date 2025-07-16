package com.bikundo.product.event_listeners;

import com.bikundo.product.dtos.RestockInventoryEvent;
import com.bikundo.product.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final ProductRepository productRepository;

    @RabbitListener(queues = "${mq.inventory.restock.queue:inventory.restock.queue}")
    @Transactional
    public void handleRestockInventory(RestockInventoryEvent event) {
        log.info("Restocking inventory for cancelled order {}", event.getOrderId());

        for (RestockInventoryEvent.Item item : event.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
                log.info("↩️ Restocked {} units of SKU {}", item.getQuantity(), product.getSku());
            });
        }
    }
}