package com.bikundo.order.repositories;

import com.bikundo.order.models.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderId(Long orderId);
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
