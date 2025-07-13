package com.bikundo.order.repositories;

import com.bikundo.order.models.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByOrderId(Long orderId);
    List<InventoryReservation> findByProductId(Long productId);
    List<InventoryReservation> findByReservationStatus(String status);
}
