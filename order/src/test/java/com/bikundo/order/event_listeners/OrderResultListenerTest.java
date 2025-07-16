package com.bikundo.order.event_listeners;

import com.bikundo.order.dtos.OrderResult;
import com.bikundo.order.models.Order;
import com.bikundo.order.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderResultListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderResultListener orderResultListener;

    private OrderResult orderResult;

    @BeforeEach
    void setUp() {
        orderResult = new OrderResult();
        orderResult.setOrderId(1L);
        orderResult.setOrderStatus(Order.OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should successfully handle order result and update order status")
    void handleOrderResult_Success() {
        // When
        orderResultListener.handleOrderResult(orderResult);

        // Then
        verify(orderService).updateOrderStatus(1L, Order.OrderStatus.CONFIRMED, null);
    }

    @Test
    @DisplayName("Should handle order result with CANCELLED status")
    void handleOrderResult_CancelledStatus() {
        // Given
        orderResult.setOrderStatus(Order.OrderStatus.CANCELLED);

        // When
        orderResultListener.handleOrderResult(orderResult);

        // Then
        verify(orderService).updateOrderStatus(1L, Order.OrderStatus.CANCELLED, null);
    }

    @Test
    @DisplayName("Should handle order result with FAILED status")
    void handleOrderResult_FailedStatus() {
        // Given
        orderResult.setOrderStatus(Order.OrderStatus.FAILED);

        // When
        orderResultListener.handleOrderResult(orderResult);

        // Then
        verify(orderService).updateOrderStatus(1L, Order.OrderStatus.FAILED, null);
    }

    @Test
    @DisplayName("Should handle exception gracefully when order service fails")
    void handleOrderResult_ServiceException() {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(orderService).updateOrderStatus(anyLong(), any(Order.OrderStatus.class), any());

        // When - should not throw exception
        orderResultListener.handleOrderResult(orderResult);

        // Then
        verify(orderService).updateOrderStatus(1L, Order.OrderStatus.CONFIRMED, null);
        // Exception should be caught and logged, not propagated
    }

    @Test
    @DisplayName("Should handle order result with different order ID")
    void handleOrderResult_DifferentOrderId() {
        // Given
        orderResult.setOrderId(999L);

        // When
        orderResultListener.handleOrderResult(orderResult);

        // Then
        verify(orderService).updateOrderStatus(999L, Order.OrderStatus.CONFIRMED, null);
    }

    @Test
    @DisplayName("Should handle null changedBy parameter correctly")
    void handleOrderResult_NullChangedBy() {
        // When
        orderResultListener.handleOrderResult(orderResult);

        // Then
        verify(orderService).updateOrderStatus(eq(1L), eq(Order.OrderStatus.CONFIRMED), isNull());
    }
}