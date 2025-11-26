package com.projectAnupam.controller;

import com.projectAnupam.dto.ApiResponse;
import com.projectAnupam.dto.CheckoutRequest;
import com.projectAnupam.dto.OrderDTO;
import com.projectAnupam.dto.ProductDTO;
import com.projectAnupam.service.OrderService;
import com.projectAnupam.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@CrossOrigin
@RequiredArgsConstructor
public class PublicController {

    private final ProductService productService;
    private final OrderService orderService;

    // ========== PRODUCT BROWSING ==========

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(required = false) Boolean newProducts,
            @RequestParam(required = false) Boolean bestSeller) {

        List<ProductDTO> products;

        // Get active products
        products = productService.getActiveProductsAndQunatityNotZero();

        // Also get inactive products that are in orders
//        List<ProductDTO> orderedInactiveProducts = productService.getInactiveProductsInOrders();

        // Combine both lists
//        Set<ProductDTO> allProducts = new HashSet<>(products);
//        allProducts.addAll(orderedInactiveProducts);
//        products = new ArrayList<>(allProducts);

        // Apply filters
        if (category != null && !category.isBlank()) {
            String categoryLower = category.toLowerCase();

            products = products.stream()
                    .filter(p -> {
                        if (p.getCategory() == null) return false;
                        String[] parts = p.getCategory().split(",");
                        for (String part : parts) {
                            if (part.trim().toLowerCase().contains(categoryLower)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        if (studentId != null) {
            products = products.stream()
                    .filter(p -> p.getStudentId().equals(studentId))
                    .collect(Collectors.toList());
        }

        if (onSale != null && onSale) {
            products = products.stream()
                    .filter(ProductDTO::getOnSale)
                    .collect(Collectors.toList());
        }

        if (newProducts != null && newProducts) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            products = products.stream()
                    .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(sevenDaysAgo))
                    .collect(Collectors.toList());
        }

        if(bestSeller!= null && bestSeller){
            // Keep only products whose ID is in bestSellerProductIds
            List<Long> bestSellerProductIds = orderService.getBestSellerProducts();

            products = products.stream()
                    .filter(p -> bestSellerProductIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    // ========== ORDER OPERATIONS ==========

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderDTO>> placeOrder(@Valid @RequestBody CheckoutRequest request) {
        OrderDTO order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping("/orders/track")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> trackOrders(@RequestParam String email) {
        List<OrderDTO> orders = orderService.getOrdersByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }
}
