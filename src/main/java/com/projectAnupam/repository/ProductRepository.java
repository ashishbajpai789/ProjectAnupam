package com.projectAnupam.repository;


import com.projectAnupam.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all active products
    List<Product> findByActiveTrue();

    // Find products by student
    List<Product> findByStudentId(Long studentId);

    // Find active products by student
    List<Product> findByStudentIdAndActiveTrue(Long studentId);

    // Find product by id and student id (for authorization)
    Optional<Product> findByIdAndStudentId(Long id, Long studentId);

    // Find products by category
    List<Product> findByActiveTrueAndCategory(String category);

    // Find products on sale
    List<Product> findByActiveTrueAndOnSaleTrue();

    // Find products by student and category
    List<Product> findByActiveTrueAndStudentIdAndCategory(Long studentId, String category);

    // Custom query for filtering products
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:studentId IS NULL OR p.student.id = :studentId) " +
            "AND (:onSale IS NULL OR p.onSale = :onSale)")
    List<Product> findProductsWithFilters(
            @Param("category") String category,
            @Param("studentId") Long studentId,
            @Param("onSale") Boolean onSale
    );

    // Get all distinct categories
    @Query(
            value = "SELECT DISTINCT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(category, ',', numbers.n), ',', -1)) AS category " +
                    "FROM product " +
                    "JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 " +
                    "      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 " +
                    "      UNION ALL SELECT 9 UNION ALL SELECT 10) numbers " +
                    "ON numbers.n <= 1 + LENGTH(category) - LENGTH(REPLACE(category, ',', '')) " +
                    "WHERE active = true " +
                    "ORDER BY category",
            nativeQuery = true
    )
    List<String> findAllIndividualCategories();

}