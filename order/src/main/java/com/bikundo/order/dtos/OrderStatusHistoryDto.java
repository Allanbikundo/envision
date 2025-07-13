package com.bikundo.order.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrderStatusHistoryDto {
    private Long id;
    private Long orderId;
    private String previousStatus;
    private String newStatus;
    private UUID changedBy;
    private String changeReason;
    private LocalDateTime createdAt;
}
