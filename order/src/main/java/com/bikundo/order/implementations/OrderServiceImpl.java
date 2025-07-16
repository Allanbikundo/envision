package com.bikundo.order.implementations;

import com.bikundo.order.config.RabbitMQConfig;
import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.mappers.OrderMapper;
import com.bikundo.order.models.Order;
import com.bikundo.order.models.Order.OrderStatus;
import com.bikundo.order.models.OrderItem;
import com.bikundo.order.models.OrderStatusHistory;
import com.bikundo.order.repositories.OrderItemRepository;
import com.bikundo.order.repositories.OrderRepository;
import com.bikundo.order.repositories.OrderStatusHistoryRepository;
import com.bikundo.order.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto placeOrder(CreateOrderRequest request, UUID userId) {


        // Step 1: Create order
        Order order = new Order();
        order.setCustomerId(userId);
        order.setChangeBy(userId);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setExternalReference(request.getExternalReference());
        order.setNotes(request.getNotes());
        order.setTotalAmount(BigDecimal.ZERO);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = orderMapper.toEntityItemList(request.getItems());
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
        }

        orderItemRepository.saveAll(items);

        // Emit event to rabbit mq
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId(order.getId());
        event.setUserId(userId);

        List<OrderPlacedEvent.Item> eventItems = items.stream().map(item -> {
            OrderPlacedEvent.Item ei = new OrderPlacedEvent.Item();
            ei.setProductId(item.getProductId());
            ei.setQuantity(item.getQuantity());
            return ei;
        }).toList();

        event.setItems(eventItems);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                event);

        // Step 5: Return DTO
        OrderDto dto = orderMapper.toDto(order);
        dto.setItems(items.stream().map(orderMapper::toDto).toList());

        return dto;
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        OrderDto dto = orderMapper.toDto(order);
        dto.setItems(items.stream().map(orderMapper::toDto).toList());

        return dto;
    }

    @Override
    public List<OrderDto> getOrdersByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByCustomerId(userId);

        return orders.stream().map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            OrderDto dto = orderMapper.toDto(order);
            dto.setItems(items.stream().map(orderMapper::toDto).toList());
            return dto;
        }).toList();
    }


    @Override
    @Transactional
    public void cancelOrder(Long id, UUID userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(userId)) {
            throw new AccessDeniedException("You cannot cancel this order.");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        orderRepository.save(order);

        updateOrderStatus(id, OrderStatus.CANCELLED, userId.toString());
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, OrderStatus newStatus, String changedBy) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));

        OrderStatus previousStatus = order.getOrderStatus();

        // Only update if the status is actually changing
        if (previousStatus != newStatus) {
            order.setOrderStatus(newStatus);
            orderRepository.save(order);

            // Record the change in history
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .changedBy(changedBy)
                    .changeReason("Updated programmatically")
                    .build();

            orderStatusHistoryRepository.save(history);
        }
    }


    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String randomPart = String.format("%04d", new Random().nextInt(10_000)); // 0000–9999
        return "ORD-" + datePart + "-" + randomPart;
    }
}
