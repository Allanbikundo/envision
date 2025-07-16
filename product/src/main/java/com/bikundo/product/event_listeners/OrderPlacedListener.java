package com.bikundo.product.event_listeners;

import com.bikundo.product.config.RabbitMQConfig;
import com.bikundo.product.dtos.OrderResult;
import com.bikundo.product.dtos.OrderEvent;
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

import static com.bikundo.product.config.RabbitMQConfig.ORDER_PLACED_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedListener {

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = ORDER_PLACED_QUEUE)
    @Transactional
    public void handleOrderPlaced(OrderEvent event) {
        log.info("Processing order placed: {}", event.getOrderId());

        try {
            // Step 1: Fetch all products involved
            List<Long> productIds = event.getItems().stream()
                    .map(OrderEvent.Item::getProductId)
                    .toList();

            Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            // Step 2: Validate stock availability
            for (OrderEvent.Item item : event.getItems()) {
                Product product = productMap.get(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }

                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for SKU: " + product.getSku());
                }
            }

            // Step 3: All good → reduce stock
            for (OrderEvent.Item item : event.getItems()) {
                Product product = productMap.get(item.getProductId());
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            }

            productRepository.saveAll(productMap.values());

            // ✅ Success → publish confirmed order result
            OrderResult orderResult = new OrderResult();
            orderResult.setOrderId(event.getOrderId());
            orderResult.setMessage("Goods acquired");
            orderResult.setOrderStatus(OrderResult.OrderStatus.CONFIRMED);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_RESULT_ROUTING_KEY,
                    orderResult
            );

        } catch (Exception ex) {
            log.error("Inventory reservation failed for order {}: {}", event.getOrderId(), ex.getMessage());

            // ❌ Failure → publish failed order result
            OrderResult orderResult = new OrderResult();
            orderResult.setOrderId(event.getOrderId());
            orderResult.setMessage(ex.getMessage());
            orderResult.setOrderStatus(OrderResult.OrderStatus.FAILED);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_RESULT_ROUTING_KEY,
                    orderResult
            );
        }
    }

}
