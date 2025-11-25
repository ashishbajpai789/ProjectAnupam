package com.projectAnupam.repository;

import com.projectAnupam.entity.Order;
import com.projectAnupam.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find order items by order id
    List<OrderItem> findByOrderId(Long orderId);

    // Find order items by student id (for student to see their sales)
    List<OrderItem> findByStudentId(Long studentId);

    // Find order items by product id
    List<OrderItem> findByProductId(Long productId);

    // Custom query to get all orders containing a student's products
    @Query("SELECT DISTINCT oi.order FROM OrderItem oi WHERE oi.student.id = :studentId ORDER BY oi.order.createdAt DESC")
    List<Order> findOrdersByStudentId(Long studentId);

    @Query(value = "SELECT product_id FROM order_items GROUP BY product_id ORDER BY COUNT(product_id) DESC LIMIT :limit",
            nativeQuery = true)
    List<Long> findTop10BestSellingProductIds(@Param("limit") int limit);

}

