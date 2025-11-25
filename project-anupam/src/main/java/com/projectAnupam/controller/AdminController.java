package com.projectAnupam.controller;

import com.projectAnupam.dto.*;
import com.projectAnupam.security.CustomUserDetails;
import com.projectAnupam.service.AdminService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin
@RequiredArgsConstructor
public class AdminController {

    private final StudentService studentService;
    private final ProductService productService;
    private final OrderService orderService;
    private final AdminService adminService;

    // ========== ADMIN MANAGEMENT ==========

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AdminDTO>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AdminDTO profile = adminService.getAdminById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AdminDTO>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        AdminDTO updated = adminService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<AdminDTO>> createAdmin(@Valid @RequestBody AdminDTO dto) {
        AdminDTO created = adminService.createAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin created successfully", created));
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminDTO>>> getAllAdmins(
            @RequestParam(required = false) Boolean active) {
        List<AdminDTO> admins;
        if (active != null) {
            admins = adminService.getAllAdminsIncludingInactive()
                    .stream()
                    .filter(a -> a.getActive().equals(active))
                    .collect(Collectors.toList());
        } else {
            admins = adminService.getAllAdminsIncludingInactive();
        }
        return ResponseEntity.ok(ApiResponse.success("Admins retrieved successfully", admins));
    }

    @PutMapping("/admins/{id}/toggle-active")
    public ResponseEntity<ApiResponse<AdminDTO>> toggleAdminActive(@PathVariable Long id) {
        AdminDTO toggled = adminService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Admin status updated successfully", toggled));
    }

    // ========== STUDENT MANAGEMENT ==========

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@Valid @RequestBody StudentDTO dto) {
        StudentDTO created = studentService.createStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created successfully", created));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getAllStudents(
            @RequestParam(required = false) Boolean active) {
        List<StudentDTO> students;
        if (active != null) {
            students = studentService.getAllStudentsIncludingInactive()
                    .stream()
                    .filter(s -> s.getActive().equals(active))
                    .collect(Collectors.toList());
        } else {
            students = studentService.getAllStudents();
        }
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
    }

    @PutMapping("/students/{id}/toggle-active")
    public ResponseEntity<ApiResponse<StudentDTO>> toggleStudentActive(@PathVariable Long id) {
        StudentDTO toggled = studentService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Student status updated successfully", toggled));
    }

    // ========== PRODUCT MANAGEMENT ==========

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        List<ProductDTO> products = productService.getAllProductsIncludingInactive();

        if (active != null) {
            products = products.stream()
                    .filter(p -> p.getActive().equals(active))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                            p.getCategory().toLowerCase().contains(searchLower) ||
                            p.getStudentName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @PutMapping("/products/{id}/toggle-active")
    public ResponseEntity<ApiResponse<ProductDTO>> toggleProductActive(@PathVariable Long id) {
        ProductDTO toggled = productService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Product status updated successfully", toggled));
    }

    // ========== ORDER MANAGEMENT ==========

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }
}