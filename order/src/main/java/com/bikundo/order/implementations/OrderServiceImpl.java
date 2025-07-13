package com.bikundo.order.implementations;

import com.bikundo.order.config.RabbitMQConfig;
import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.dtos.OrderItemDto;
import com.bikundo.order.mappers.OrderMapper;
import com.bikundo.order.models.Order;
import com.bikundo.order.models.Order.OrderStatus;
import com.bikundo.order.models.OrderItem;
import com.bikundo.order.repositories.AddressRepository;
import com.bikundo.order.repositories.OrderItemRepository;
import com.bikundo.order.repositories.OrderRepository;
import com.bikundo.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bikundo.order.models.Address;
import java.math.BigDecimal;
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
    private final AddressRepository addressRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto placeOrder(CreateOrderRequest request, UUID userId) {
        // Step 1: Validate addresses
        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid shipping address ID"));

        Address billingAddress = addressRepository.findById(request.getBillingAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid billing address ID"));

        // Step 2: Create order
        Order order = new Order();
        order.setCustomerId(userId);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.setExternalReference(request.getExternalReference());
        order.setNotes(request.getNotes());

        Order savedOrder = orderRepository.save(order);
        
        List<OrderItem> items = orderMapper.toEntityItemList(request.getItems());

        for (OrderItem item : items) {
            item.setOrder(order);
            item.setTotalPrice(
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }
        

        orderItemRepository.saveAll(items);

        // Step 4: Emit event (outside transaction if needed, see note below)
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId(order.getId());
        event.setUserId(userId);
        event.setOrderNumber(order.getOrderNumber());
        event.setShippingAddressId(shippingAddress.getId());
        event.setBillingAddressId(billingAddress.getId());
        event.setExternalReference(order.getExternalReference());

        List<OrderPlacedEvent.Item> eventItems = items.stream().map(item -> {
            OrderPlacedEvent.Item ei = new OrderPlacedEvent.Item();
            ei.setProductId(item.getProductId());
            ei.setProductSku(item.getProductSku());
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOrderById'");
    }

    @Override
    public List<OrderDto> getOrdersByUserId(UUID userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOrdersByUserId'");
    }

    @Override
    public void cancelOrder(Long id, UUID userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelOrder'");
    }

    @Override
    public void updateOrderStatus(Long id, OrderStatus status, String changedBy) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateOrderStatus'");
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String randomPart = String.format("%04d", new Random().nextInt(10_000)); // 0000–9999
        return "ORD-" + datePart + "-" + randomPart;
    }
}
