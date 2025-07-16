package com.bikundo.order.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.services.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management APIs")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order", description = "Creates an order with one or more products and emits an OrderPlacedEvent", responses = {
            @ApiResponse(responseCode = "200", description = "Order created successfully", content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    }, security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<OrderDto> placeOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        OrderDto response = orderService.placeOrder(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an order by ID")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.cancelOrder(id, userId);
        return ResponseEntity.noContent().build();
    }
}
