package com.bikundo.order;

import com.bikundo.order.controllers.OrderController;
import com.bikundo.order.dtos.CreateOrderItemRequest;
import com.bikundo.order.dtos.CreateOrderRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.dtos.OrderItemDto;
import com.bikundo.order.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();




    private Jwt getMockJwt() {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", userId.toString())
                .claim("resource_access", Map.of(
                    "order-client", Map.of(
                        "roles", List.of("customer")
                    )
                ))
                .build();
    }

    private Jwt getMockJwtWithoutCustomerRole() {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", userId.toString())
                .claim("resource_access", Map.of(
                    "order-client", Map.of(
                        "roles", List.of("admin") // Different role
                    )
                ))
                .build();
    }

    private CreateOrderRequest createValidRequest() {
        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setContactEmail("test@example.com");
        req.setContactPhone("0700000000");
        req.setExternalReference("WEB-ORDER-001");
        req.setItems(List.of(item));

        return req;
    }

    private OrderDto dummyResponse() {
        OrderDto dto = new OrderDto();
        dto.setId(10L);
        dto.setOrderNumber("ORD-0001");
        dto.setTotalAmount(BigDecimal.valueOf(108000));
        dto.setItems(List.of());

        return dto;
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns order")
    void testGetOrderById() throws Exception {
        OrderItemDto orderItem = OrderItemDto.builder()
                .id(1L)
                .productId(1L)
                .quantity(1)
                .build();
        OrderDto order = new OrderDto();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(1000));
        order.setItems(List.of(orderItem));

        Mockito.when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1")
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} cancels order for user")
    void testCancelOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwt())))
                .andExpect(status().isNoContent());

        Mockito.verify(orderService).cancelOrder(eq(1L), eq(userId));
    }


    @Test
    @DisplayName("POST /api/orders - should create order successfully")
    void testPlaceOrderSuccess() throws Exception {
        CreateOrderRequest request = createValidRequest();
        OrderDto response = dummyResponse();

        Mockito.when(orderService.placeOrder(eq(request), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-0001"));
    }

    @Test
    @DisplayName("POST /api/orders - should return 401 if unauthenticated")
    void testPlaceOrderUnauthorized() throws Exception {
        CreateOrderRequest request = createValidRequest();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // In @WebMvcTest, unauthenticated requests return 403
    }

    @Test
    @DisplayName("POST /api/orders - should return 400 for invalid input")
    void testPlaceOrderInvalidInput() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(); // missing required fields

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwt())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders - should return 500 on unexpected service failure")
    void testPlaceOrderServiceFailure() throws Exception {
        CreateOrderRequest request = createValidRequest();

        Mockito.when(orderService.placeOrder(any(), eq(userId)))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwt())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    @Test
    @DisplayName("POST /api/orders - should work with any authenticated user (temporary)")
    void testPlaceOrderWithAnyAuthenticatedUser() throws Exception {
        CreateOrderRequest request = createValidRequest();
        OrderDto response = dummyResponse();

        Mockito.when(orderService.placeOrder(eq(request), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwtWithoutCustomerRole())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - should work with any authenticated user (temporary)")
    void testGetOrderByIdWithAnyAuthenticatedUser() throws Exception {
        OrderItemDto orderItem = OrderItemDto.builder()
                .id(1L)
                .productId(1L)
                .quantity(1)
                .build();
        OrderDto order = new OrderDto();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(1000));
        order.setItems(List.of(orderItem));

        Mockito.when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwtWithoutCustomerRole())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - should work with any authenticated user (temporary)")
    void testCancelOrderWithAnyAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(getMockJwtWithoutCustomerRole())))
                .andExpect(status().isNoContent());

        Mockito.verify(orderService).cancelOrder(eq(1L), eq(userId));
    }

}
