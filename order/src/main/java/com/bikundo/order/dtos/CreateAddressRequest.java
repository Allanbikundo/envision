package com.bikundo.order.dtos;

import com.bikundo.order.models.Address.AddressType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Payload to create a new address")
public class CreateAddressRequest {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String country;
        private AddressType addressType; // SHIPPING, BILLING, BOTH
    }
    