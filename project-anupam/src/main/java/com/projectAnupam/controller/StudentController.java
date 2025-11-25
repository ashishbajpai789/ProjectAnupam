package com.projectAnupam.controller;

import com.projectAnupam.dto.*;
import com.projectAnupam.security.CustomUserDetails;
import com.projectAnupam.service.OrderService;
import com.projectAnupam.service.ProductService;
import com.projectAnupam.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@CrossOrigin
@RequiredArgsConstructor
public class StudentController {

    private final ProductService productService;
    private final OrderService orderService;
    private final StudentService studentService;

    // ========== PROFILE MANAGEMENT ==========

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<StudentDTO>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        StudentDTO profile = studentService.getStudentById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<StudentDTO>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        StudentDTO updated = studentService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    // ========== PRODUCT MANAGEMENT ==========

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute ProductDTO dto) {
        System.err.println(dto);
        ProductDTO created = productService. createProduct(userDetails.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProductDTO> products = productService.getStudentProducts(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @ModelAttribute ProductDTO dto) {
        ProductDTO updated = productService.updateProduct(userDetails.getId(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @PutMapping("/products/{id}/toggle-active")
    public ResponseEntity<ApiResponse<ProductDTO>> toggleProductActive(@PathVariable Long id) {
        ProductDTO toggled = productService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Product status updated successfully", toggled));
    }

    // ========== ORDER MANAGEMENT ==========

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderDTO> orders = orderService.getStudentOrders(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderDTO updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updated));
    }
}