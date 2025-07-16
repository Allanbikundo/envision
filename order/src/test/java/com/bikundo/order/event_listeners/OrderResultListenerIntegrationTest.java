package com.bikundo.order.event_listeners;

import com.bikundo.order.dtos.OrderResult;
import com.bikundo.order.models.Order;
import com.bikundo.order.services.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static com.bikundo.order.config.RabbitMQConfig.ORDER_RESULT_QUEUE;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class OrderResultListenerIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("Should process OrderResult message from RabbitMQ queue")
    void shouldProcessOrderResultMessage() throws Exception {
        // Given
        OrderResult orderResult = new OrderResult();
        orderResult.setOrderId(123L);
        orderResult.setOrderStatus(Order.OrderStatus.CONFIRMED);

        // When
        rabbitTemplate.convertAndSend(ORDER_RESULT_QUEUE, orderResult);

        // Then - Wait and verify the message was processed
        Thread.sleep(2000); // Simple wait for message processing
        verify(orderService, timeout(5000)).updateOrderStatus(123L, Order.OrderStatus.CONFIRMED, null);
    }

    @Test
    @DisplayName("Should process multiple OrderResult messages")
    void shouldProcessMultipleMessages() throws Exception {
        // Given
        OrderResult result1 = new OrderResult();
        result1.setOrderId(100L);
        result1.setOrderStatus(Order.OrderStatus.CONFIRMED);

        OrderResult result2 = new OrderResult();
        result2.setOrderId(200L);
        result2.setOrderStatus(Order.OrderStatus.FAILED);

        // When
        rabbitTemplate.convertAndSend(ORDER_RESULT_QUEUE, result1);
        rabbitTemplate.convertAndSend(ORDER_RESULT_QUEUE, result2);

        // Then
        verify(orderService, timeout(5000)).updateOrderStatus(100L, Order.OrderStatus.CONFIRMED, null);
        verify(orderService, timeout(5000)).updateOrderStatus(200L, Order.OrderStatus.FAILED, null);
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void shouldHandleServiceException() throws Exception {
        // Given
        OrderResult orderResult = new OrderResult();
        orderResult.setOrderId(999L);
        orderResult.setOrderStatus(Order.OrderStatus.CONFIRMED);

        doThrow(new RuntimeException("Service error"))
                .when(orderService).updateOrderStatus(999L, Order.OrderStatus.CONFIRMED, null);

        // When
        rabbitTemplate.convertAndSend(ORDER_RESULT_QUEUE, orderResult);

        // Then - Should still attempt to call the service
        verify(orderService, timeout(5000)).updateOrderStatus(999L, Order.OrderStatus.CONFIRMED, null);
    }
}