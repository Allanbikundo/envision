package com.bikundo.order.services;

import java.util.List;

import com.bikundo.order.dtos.InventoryReservationRequest;

public interface InventoryReservationService {
    void reserveStock(Long orderId, List<InventoryReservationRequest> items);
    void releaseStock(Long orderId, String reason);
    void confirmReservation(Long orderId);
    int cleanupExpiredReservations();
}

