package com.bikundo.order.services;

import java.util.List;

import com.bikundo.order.dtos.AddressDto;
import com.bikundo.order.dtos.CreateAddressRequest;

public interface AddressService {
    AddressDto createAddress(CreateAddressRequest request);
    List<AddressDto> getAll();
    AddressDto getById(Long id);
}
