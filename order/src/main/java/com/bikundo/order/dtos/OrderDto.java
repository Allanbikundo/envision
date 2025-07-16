package com.bikundo.order.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private UUID customerId;
    private String orderStatus;
    private BigDecimal totalAmount;
    private String currency;
    private String externalReference;
    private String contactEmail;
    private String contactPhone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemDto> items;
}
