package com.bikundo.order.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bikundo.order.dtos.AddressDto;
import com.bikundo.order.dtos.CreateAddressRequest;
import com.bikundo.order.services.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management API")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDto> create(@RequestBody CreateAddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(request));
    }

    @GetMapping
    public ResponseEntity<List<AddressDto>> getAll() {
        return ResponseEntity.ok(addressService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getById(id));
    }
}

