package com.bikundo.product.event_listeners;

import com.bikundo.product.config.RabbitMQConfig;
import com.bikundo.product.dtos.OrderResult;
import com.bikundo.product.dtos.RestockInventoryEvent;
import com.bikundo.product.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.bikundo.product.config.RabbitMQConfig.ORDER_CANCEL_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = ORDER_CANCEL_QUEUE)
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

        log.info("cancelling order {}" , event.getOrderId());
        // send an event to update
        OrderResult orderResult = new OrderResult();
        orderResult.setOrderId(event.getOrderId());
        orderResult.setMessage("order cancelled");
        orderResult.setOrderStatus(OrderResult.OrderStatus.CANCELLED);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_RESULT_ROUTING_KEY,
                orderResult
        );
    }
}