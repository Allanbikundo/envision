package com.bikundo.order.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderItemRequest {
    private Long productId;
    private String productSku;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
