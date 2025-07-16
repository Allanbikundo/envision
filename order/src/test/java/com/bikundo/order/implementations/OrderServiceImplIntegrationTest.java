package com.bikundo.order.implementations;

import com.bikundo.order.dtos.CreateOrderItemRequest;
import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.models.Order;
import com.bikundo.order.models.OrderItem;
import com.bikundo.order.models.OrderStatusHistory;
import com.bikundo.order.repositories.OrderItemRepository;
import com.bikundo.order.repositories.OrderRepository;
import com.bikundo.order.repositories.OrderStatusHistoryRepository;
import com.bikundo.order.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class OrderServiceImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate; // Mock to avoid RabbitMQ dependency

    private UUID userId;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setContactEmail("test@example.com");
        createOrderRequest.setContactPhone("0700000000");
        createOrderRequest.setExternalReference("WEB-ORDER-001");
        createOrderRequest.setNotes("Integration test order");
        createOrderRequest.setItems(List.of(itemRequest));
    }

    @Test
    @DisplayName("Should place order and persist to database")
    void placeOrder_DatabaseIntegration() {
        // When
        OrderDto result = orderService.placeOrder(createOrderRequest, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOrderNumber()).startsWith("ORD-");
        assertThat(result.getItems()).hasSize(1);

        // Verify order was persisted
        Order savedOrder = orderRepository.findById(result.getId()).orElse(null);
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getCustomerId()).isEqualTo(userId);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(savedOrder.getExternalReference()).isEqualTo("WEB-ORDER-001");
        assertThat(savedOrder.getNotes()).isEqualTo("Integration test order");

        // Verify order items were persisted
        List<OrderItem> savedItems = orderItemRepository.findByOrderId(result.getId());
        assertThat(savedItems).hasSize(1);
        assertThat(savedItems.get(0).getProductId()).isEqualTo(1L);
        assertThat(savedItems.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should retrieve order with items from database")
    void getOrderById_DatabaseIntegration() {
        // Given - place an order first
        OrderDto placedOrder = orderService.placeOrder(createOrderRequest, userId);

        // When
        OrderDto retrievedOrder = orderService.getOrderById(placedOrder.getId());

        // Then
        assertThat(retrievedOrder).isNotNull();
        assertThat(retrievedOrder.getId()).isEqualTo(placedOrder.getId());
        assertThat(retrievedOrder.getOrderNumber()).isEqualTo(placedOrder.getOrderNumber());
        assertThat(retrievedOrder.getItems()).hasSize(1);
        assertThat(retrievedOrder.getItems().get(0).getProductId()).isEqualTo(1L);
        assertThat(retrievedOrder.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException for non-existent order")
    void getOrderById_NotFound() {
        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    @DisplayName("Should cancel confirmed order successfully")
    void cancelOrder_DatabaseIntegration() {
        // Given - place and confirm an order
        OrderDto placedOrder = orderService.placeOrder(createOrderRequest, userId);
        orderService.updateOrderStatus(placedOrder.getId(), Order.OrderStatus.CONFIRMED, "system");

        // When
        orderService.cancelOrder(placedOrder.getId(), userId);

        // Then
        Order cancelledOrder = orderRepository.findById(placedOrder.getId()).orElse(null);
        assertThat(cancelledOrder).isNotNull();
        assertThat(cancelledOrder.getOrderStatus()).isEqualTo(Order.OrderStatus.CANCELLED);

        // Verify status history was recorded
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(placedOrder.getId());
        assertThat(history).hasSizeGreaterThanOrEqualTo(2); // PENDING->CONFIRMED, CONFIRMED->CANCELLED
        
        OrderStatusHistory latestHistory = history.get(0);
        assertThat(latestHistory.getPreviousStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        assertThat(latestHistory.getNewStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when cancelling another user's order")
    void cancelOrder_AccessDenied() {
        // Given - place order with one user
        OrderDto placedOrder = orderService.placeOrder(createOrderRequest, userId);
        orderService.updateOrderStatus(placedOrder.getId(), Order.OrderStatus.CONFIRMED, "system");

        // When & Then - try to cancel with different user
        UUID differentUserId = UUID.randomUUID();
        assertThatThrownBy(() -> orderService.cancelOrder(placedOrder.getId(), differentUserId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You cannot cancel this order.");
    }

    @Test
    @DisplayName("Should update order status and record history")
    void updateOrderStatus_DatabaseIntegration() {
        // Given - place an order
        OrderDto placedOrder = orderService.placeOrder(createOrderRequest, userId);

        // When
        orderService.updateOrderStatus(placedOrder.getId(), Order.OrderStatus.CONFIRMED, "payment-service");

        // Then
        Order updatedOrder = orderRepository.findById(placedOrder.getId()).orElse(null);
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);

        // Verify status history was recorded
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(placedOrder.getId());
        assertThat(history).hasSize(1);
        
        OrderStatusHistory statusHistory = history.get(0);
        assertThat(statusHistory.getPreviousStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(statusHistory.getNewStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        assertThat(statusHistory.getOrder().getId()).isEqualTo(placedOrder.getId());
    }

    @Test
    @DisplayName("Should not create duplicate status history for same status")
    void updateOrderStatus_SameStatus() {
        // Given - place an order
        OrderDto placedOrder = orderService.placeOrder(createOrderRequest, userId);

        // When - update to same status
        orderService.updateOrderStatus(placedOrder.getId(), Order.OrderStatus.PENDING, "system");

        // Then - no status history should be created
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(placedOrder.getId());
        assertThat(history).isEmpty();
    }

    @Test
    @DisplayName("Should generate unique order numbers")
    void placeOrder_UniqueOrderNumbers() {
        // Given - create multiple orders
        CreateOrderRequest request1 = createOrderRequest;
        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setContactEmail("test2@example.com");
        request2.setContactPhone("0700000001");
        request2.setItems(createOrderRequest.getItems());

        // When
        OrderDto order1 = orderService.placeOrder(request1, userId);
        OrderDto order2 = orderService.placeOrder(request2, userId);

        // Then
        assertThat(order1.getOrderNumber()).isNotEqualTo(order2.getOrderNumber());
        assertThat(order1.getOrderNumber()).startsWith("ORD-");
        assertThat(order2.getOrderNumber()).startsWith("ORD-");
    }
}