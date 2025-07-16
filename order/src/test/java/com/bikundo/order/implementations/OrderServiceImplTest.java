package com.bikundo.order.implementations;

import com.bikundo.order.dtos.CreateOrderItemRequest;
import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.dtos.OrderEvent;
import com.bikundo.order.mappers.OrderMapper;
import com.bikundo.order.models.Order;
import com.bikundo.order.models.OrderItem;
import com.bikundo.order.models.OrderStatusHistory;
import com.bikundo.order.repositories.OrderItemRepository;
import com.bikundo.order.repositories.OrderRepository;
import com.bikundo.order.repositories.OrderStatusHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bikundo.order.config.RabbitMQConfig.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private CreateOrderRequest createOrderRequest;
    private Order savedOrder;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        // Setup create order request
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setContactEmail("test@example.com");
        createOrderRequest.setContactPhone("0700000000");
        createOrderRequest.setExternalReference("WEB-ORDER-001");
        createOrderRequest.setNotes("Test order");
        createOrderRequest.setItems(List.of(itemRequest));

        // Setup saved order
        savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-20250716-1234")
                .customerId(userId)
                .orderStatus(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .externalReference("WEB-ORDER-001")
                .notes("Test order")
                .build();

        // Setup order items
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(savedOrder)
                .productId(1L)
                .quantity(2)
                .build();
        orderItems = List.of(orderItem);
    }

    @Test
    @DisplayName("Should successfully place an order")
    void placeOrder_Success() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toEntityItemList(createOrderRequest.getItems())).thenReturn(orderItems);
        when(orderItemRepository.saveAll(orderItems)).thenReturn(orderItems);
        
        OrderDto expectedDto = new OrderDto();
        expectedDto.setId(1L);
        expectedDto.setOrderNumber("ORD-20250716-1234");
        
        when(orderMapper.toDto(savedOrder)).thenReturn(expectedDto);
        when(orderMapper.toDto(any(OrderItem.class))).thenReturn(new com.bikundo.order.dtos.OrderItemDto());

        // When
        OrderDto result = orderService.placeOrder(createOrderRequest, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-20250716-1234");

        // Verify order was saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getCustomerId()).isEqualTo(userId);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(capturedOrder.getExternalReference()).isEqualTo("WEB-ORDER-001");

        // Verify items were saved
        verify(orderItemRepository).saveAll(orderItems);

        // Verify event was published
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(ORDER_EXCHANGE), eq(ORDER_PLACED_ROUTING_KEY), eventCaptor.capture());
        OrderEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(1L);
        assertThat(capturedEvent.getUserId()).isEqualTo(userId);
        assertThat(capturedEvent.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(orderItems);
        
        OrderDto expectedDto = new OrderDto();
        expectedDto.setId(1L);
        when(orderMapper.toDto(savedOrder)).thenReturn(expectedDto);
        when(orderMapper.toDto(any(OrderItem.class))).thenReturn(new com.bikundo.order.dtos.OrderItemDto());

        // When
        OrderDto result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).findById(1L);
        verify(orderItemRepository).findByOrderId(1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when order not found")
    void getOrderById_NotFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    @DisplayName("Should successfully cancel order")
    void cancelOrder_Success() {
        // Given
        Order confirmedOrder = Order.builder()
                .id(1L)
                .customerId(userId)
                .orderStatus(Order.OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(confirmedOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(orderItems);

        // When
        orderService.cancelOrder(1L, userId);

        // Then
        // Verify cancel event was published
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(ORDER_EXCHANGE), eq(ORDER_CANCEL_ROUTING_KEY), eventCaptor.capture());
        OrderEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(1L);
        assertThat(capturedEvent.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when cancelling non-existent order")
    void cancelOrder_OrderNotFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user tries to cancel another user's order")
    void cancelOrder_AccessDenied() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        Order otherUserOrder = Order.builder()
                .id(1L)
                .customerId(differentUserId)
                .orderStatus(Order.OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(otherUserOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L, userId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You cannot cancel this order.");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to cancel non-confirmed order")
    void cancelOrder_InvalidStatus() {
        // Given
        Order pendingOrder = Order.builder()
                .id(1L)
                .customerId(userId)
                .orderStatus(Order.OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only confirmed orders can be cancelled.");
    }

    @Test
    @DisplayName("Should successfully update order status")
    void updateOrderStatus_Success() {
        // Given
        Order existingOrder = Order.builder()
                .id(1L)
                .customerId(userId)
                .orderStatus(Order.OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        // When
        orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED, "system");

        // Then
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order updatedOrder = orderCaptor.getValue();
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);

        // Verify status history was recorded
        ArgumentCaptor<OrderStatusHistory> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository).save(historyCaptor.capture());
        OrderStatusHistory history = historyCaptor.getValue();
        assertThat(history.getPreviousStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(history.getNewStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should not update order status if status is the same")
    void updateOrderStatus_SameStatus() {
        // Given
        Order existingOrder = Order.builder()
                .id(1L)
                .customerId(userId)
                .orderStatus(Order.OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));

        // When
        orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED, "system");

        // Then
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderStatusHistoryRepository, never()).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating non-existent order status")
    void updateOrderStatus_OrderNotFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED, "system"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found with ID: 1");
    }
}