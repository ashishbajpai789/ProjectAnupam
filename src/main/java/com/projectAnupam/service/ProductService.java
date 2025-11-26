package com.projectAnupam.service;


import com.projectAnupam.dto.ProductDTO;
import com.projectAnupam.entity.Product;
import com.projectAnupam.entity.Student;
import com.projectAnupam.exception.BadRequestException;
import com.projectAnupam.exception.ResourceNotFoundException;
import com.projectAnupam.exception.UnauthorizedException;
import com.projectAnupam.global.Util;
import com.projectAnupam.repository.OrderRepository;
import com.projectAnupam.repository.ProductRepository;
import com.projectAnupam.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StudentRepository studentRepository;
    private final OrderRepository orderRepository;
    private final Util util;

    // ========== STUDENT OPERATIONS ==========

    @Transactional
    public ProductDTO createProduct(Long studentId, ProductDTO dto) {
        Student student = studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        validateProductData(dto);

        Product product = new Product();
        product.setStudent(student);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(util.capitalizeCategories(dto.getCategory()));
        product.setActive(true);
        product.setOnSale(dto.getOnSale() != null ? dto.getOnSale() : false);
        product.setSalePrice(dto.getSalePrice());

        // Handle file upload
        MultipartFile image = dto.getImageFile();
        System.err.println("image -------------"+image);
        if (image != null && !image.isEmpty()) {
            try {

                Path uploadPath = Paths.get("uploads");
                Files.createDirectories(uploadPath);

                String original = image.getOriginalFilename();
                String extension = original.substring(original.lastIndexOf("."));
                String studentName = student.getName().replaceAll("\\s+", "_");
                String productName = dto.getName().replaceAll("\\s+", "_");
                String fileName = studentName + "_" + productName + extension;
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                product.setImage("/uploads/" + fileName); // store relative path
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    public List<ProductDTO> getStudentProducts(Long studentId) {
        return productRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO updateProduct(Long studentId, Long productId, ProductDTO dto) {
        Product product = productRepository.findByIdAndStudentId(productId, studentId)
                .orElseThrow(() -> new UnauthorizedException("You can only update your own products"));

        validateProductData(dto);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(util.capitalizeCategories(dto.getCategory()));

        MultipartFile image = dto.getImageFile();
        if (image != null && !image.isEmpty()) {
            try {

                Path uploadPath = Paths.get("uploads");
                Files.createDirectories(uploadPath);

                String original = image.getOriginalFilename();
                String extension = original.substring(original.lastIndexOf("."));
                String studentName = product.getStudent().getName().replaceAll("\\s+", "_");
                String productName = dto.getName().replaceAll("\\s+", "_");
                String fileName = studentName + "_" + productName + extension;
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                product.setImage("/uploads/" + fileName); // store relative path
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        // Handle on sale logic
        if (dto.getOnSale() != null) {
            product.setOnSale(dto.getOnSale());
            if (dto.getOnSale() && dto.getSalePrice() != null) {
                product.setSalePrice(dto.getSalePrice());
            } else if (!dto.getOnSale()) {
                product.setSalePrice(null);
            }
        }

        // Auto-deactivate if quantity is 0
        if (dto.getQuantity() == 0) {
            product.setActive(false);
        } else if (dto.getQuantity() > 0 && !product.getActive()) {
            product.setActive(true);
        }

        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    // ========== PUBLIC OPERATIONS ==========

    public List<ProductDTO> getActiveProductsAndQuantityNotZero() {
        return productRepository.findByActiveTrueAndQuantityGreaterThan(0).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return convertToDTO(product);
    }

    public List<String> getAllCategories() {
        return productRepository.findAllIndividualCategories();
    }

    // ========== HELPER METHODS ==========

    private void validateProductData(ProductDTO dto) {
        if (dto.getOnSale() != null && dto.getOnSale()) {
            if (dto.getSalePrice() == null) {
                throw new BadRequestException("Sale price is required when product is on sale");
            }
            if (dto.getSalePrice().compareTo(dto.getPrice()) >= 0) {
                throw new BadRequestException("Sale price must be less than regular price");
            }
        }
    }

    public List<ProductDTO> getAllProductsIncludingInactive() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO toggleActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if student is active before activating product
        if (!product.getActive() && !product.getStudent().getActive()) {
            throw new BadRequestException("Cannot activate product. Student is inactive.");
        }

        product.setActive(!product.getActive());
        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    public List<ProductDTO> getInactiveProductsInOrders() {
        // Get all product IDs that are in orders
        List<Long> orderedProductIds = orderRepository.findAll().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> item.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        // Get inactive products that have been ordered
        return productRepository.findAllById(orderedProductIds).stream()
                .filter(p -> !p.getActive())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setStudentId(product.getStudent().getId());
        dto.setStudentName(product.getStudent().getName());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setCategory(product.getCategory());
        dto.setImage(product.getImage());
        dto.setActive(product.getActive());
        dto.setOnSale(product.getOnSale());
        dto.setSalePrice(product.getSalePrice());
        dto.setEffectivePrice(product.getEffectivePrice());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}