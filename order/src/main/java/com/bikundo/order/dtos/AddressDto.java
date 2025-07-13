package com.bikundo.order.dtos;

import lombok.Data;
import java.time.Instant;

import com.bikundo.order.models.Address.AddressType;

@Data
public class AddressDto {
    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;
    private AddressType addressType;
    private Instant createdAt;
    private Instant updatedAt;
}