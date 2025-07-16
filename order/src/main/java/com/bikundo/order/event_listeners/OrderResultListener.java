package com.bikundo.order.event_listeners;


import com.bikundo.order.dtos.OrderResult;
import com.bikundo.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.bikundo.order.config.RabbitMQConfig.ORDER_RESULT_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderResultListener {


    private final OrderService orderService;

    @RabbitListener(queues = ORDER_RESULT_QUEUE)
    public void handleOrderResult(OrderResult event) {
        log.info("Order {} : {}", event.getOrderStatus().name(),  event.getOrderId());
        try {
            orderService.updateOrderStatus(event.getOrderId(), event.getOrderStatus(), null);
        } catch (Exception ex) {
            log.error("Failed to mark order {} as CONFIRMED", event.getOrderId(), ex);
        }
    }
}
