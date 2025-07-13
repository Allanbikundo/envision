package com.bikundo.order.dtos;

import org.mapstruct.Mapper;

import com.bikundo.order.models.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toEntity(CreateAddressRequest request);
    AddressDto toDto(Address address);
}

