package com.projectAnupam.repository;

import com.projectAnupam.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Admin> findByIdAndActiveTrue(Long id);

    // ✅ NEW: Count active admins
    long countByActiveTrue();

    // ✅ NEW: Find all active admins
    List<Admin> findByActiveTrue();

    // ✅ NEW: Find by email and active
    Optional<Admin> findByEmailAndActiveTrue(String email);
}

