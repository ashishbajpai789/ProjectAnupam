package com.projectAnupam.service;

import com.projectAnupam.dto.*;
import com.projectAnupam.entity.Order;
import com.projectAnupam.entity.OrderItem;
import com.projectAnupam.entity.OrderStatus;
import com.projectAnupam.entity.Product;
import com.projectAnupam.exception.InsufficientStockException;
import com.projectAnupam.exception.ResourceNotFoundException;
import com.projectAnupam.repository.OrderItemRepository;
import com.projectAnupam.repository.OrderRepository;
import com.projectAnupam.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    // ========== CUSTOMER OPERATIONS ==========

    @Transactional
    public OrderDTO placeOrder(CheckoutRequest request) {
        // Create order
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setAddress(request.getAddress());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process cart items
        for (CartItemDTO cartItem : request.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

            // Check stock
            if (!product.getActive()) {
                throw new InsufficientStockException("Product is not available: " + product.getName());
            }

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + product.getName() + ". Available: " + product.getQuantity()
                );
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setStudent(product.getStudent());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getEffectivePrice()); // Use effective price (sale or regular)

            order.addOrderItem(orderItem);

            // Update product quantity
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());

            // Deactivate product if out of stock
            if (product.getQuantity() == 0) {
                product.setActive(false);
            }

            productRepository.save(product);

            // Calculate total
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public List<OrderDTO> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== STUDENT OPERATIONS ==========

    public List<OrderDTO> getStudentOrders(Long studentId) {
        List<Order> orders = orderItemRepository.findOrdersByStudentId(studentId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== ADMIN OPERATIONS ==========

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            Order updated = orderRepository.save(order);
            return convertToDTO(updated);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid order status: " + status);
        }
    }

    public List<Long> getBestSellerProducts() {
        return orderItemRepository.findTop10BestSellingProductIds(15);
    }

    // ========== HELPER METHODS ==========

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setAddress(order.getAddress());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());
        dto.setOrderItems(items);

        return dto;
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImageUrl(item.getProduct().getImage());
        dto.setStudentId(item.getStudent().getId());
        dto.setStudentName(item.getStudent().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
