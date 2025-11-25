package com.projectAnupam.repository;

import com.projectAnupam.entity.Order;
import com.projectAnupam.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by customer email
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    // Find orders by status
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    // Find all orders ordered by creation date
    List<Order> findAllByOrderByCreatedAtDesc();

    // Find orders created between dates
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Count orders by status
    long countByStatus(OrderStatus status);
}
