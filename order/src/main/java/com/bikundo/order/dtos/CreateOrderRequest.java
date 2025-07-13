package com.bikundo.order.dtos;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String externalReference;
    private String contactEmail;
    private String contactPhone;
    private Long shippingAddressId;
    private Long billingAddressId;
    private String notes;
    private List<CreateOrderItemRequest> items;
}
