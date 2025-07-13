package com.bikundo.order.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.bikundo.order.dtos.CreateOrderItemRequest;
import com.bikundo.order.dtos.OrderDto;
import com.bikundo.order.dtos.OrderItemDto;
import com.bikundo.order.models.Order;
import com.bikundo.order.models.OrderItem;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "cancelledAt", source = "cancelledAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "shippedAt", source = "shippedAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "deliveredAt", source = "deliveredAt", qualifiedByName = "instantToLocalDateTime")
    @Mapping(target = "customerId", source = "customerId", qualifiedByName = "identityUuid")
    OrderDto toDto(Order order);

    List<OrderItemDto> toItemDtoList(List<OrderItem> items);

    List<OrderItem> toEntityItemList(List<CreateOrderItemRequest> items);

    OrderItem toEntity(CreateOrderItemRequest request);


    OrderItemDto toDto(OrderItem item);

    @Named("instantToLocalDateTime")
    static LocalDateTime mapInstant(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Named("identityUuid")
    default UUID identity(UUID value) {
        return value;
    }
}

