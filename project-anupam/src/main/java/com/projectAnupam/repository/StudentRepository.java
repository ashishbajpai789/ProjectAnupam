package com.projectAnupam.repository;

import com.projectAnupam.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Student> findByIdAndActiveTrue(Long id);

    // ✅ NEW: Find all active students
    List<Student> findByActiveTrue();

    // ✅ NEW: Find by email and active
    Optional<Student> findByEmailAndActiveTrue(String email);
}
