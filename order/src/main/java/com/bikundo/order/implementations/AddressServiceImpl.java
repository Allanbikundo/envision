package com.bikundo.order.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bikundo.order.dtos.AddressDto;
import com.bikundo.order.dtos.AddressMapper;
import com.bikundo.order.dtos.CreateAddressRequest;
import com.bikundo.order.models.Address;
import com.bikundo.order.repositories.AddressRepository;
import com.bikundo.order.services.AddressService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressDto createAddress(CreateAddressRequest request) {
        Address address = addressMapper.toEntity(request);
        address = addressRepository.save(address);
        return addressMapper.toDto(address);
    }

    @Override
    public List<AddressDto> getAll() {
        return addressRepository.findAll().stream()
            .map(addressMapper::toDto)
            .toList();
    }

    @Override
    public AddressDto getById(Long id) {
        Address address = addressRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        return addressMapper.toDto(address);
    }
}

