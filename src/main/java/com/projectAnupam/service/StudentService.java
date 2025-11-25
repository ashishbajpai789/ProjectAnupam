package com.projectAnupam.service;

import com.projectAnupam.dto.StudentDTO;
import com.projectAnupam.dto.UpdateProfileRequest;
import com.projectAnupam.entity.Product;
import com.projectAnupam.entity.Student;
import com.projectAnupam.exception.BadRequestException;
import com.projectAnupam.exception.ResourceNotFoundException;
import com.projectAnupam.global.Util;
import com.projectAnupam.repository.ProductRepository;
import com.projectAnupam.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final Util util;

    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {
        // Check if email already exists
        if (util.emailAlreadyInUse(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        Student student = new Student();
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPassword(passwordEncoder.encode(dto.getPassword()));
        student.setPhone(dto.getPhone());
        student.setActive(true);

        Student saved = studentRepository.save(student);
        return convertToDTO(saved);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return convertToDTO(student);
    }

    @Transactional
    public StudentDTO updateProfile(Long studentId, UpdateProfileRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            student.setName(request.getName());
        }

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!student.getEmail().equals(request.getEmail()) &&
                    util.emailAlreadyInUse(request.getEmail())) {
                throw new BadRequestException("Email already registered");
            }
            student.setEmail(request.getEmail());
        }

        // Update phone if provided
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }

        // Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to change password");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), student.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }

            student.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        Student updated = studentRepository.save(student);
        return convertToDTO(updated);
    }

    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setActive(student.getActive());
        dto.setCreatedAt(student.getCreatedAt());
        return dto;
    }

    public List<StudentDTO> getAllStudentsIncludingInactive() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentDTO toggleActive(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setActive(!student.getActive());
        Student updated = studentRepository.save(student);

        // If deactivating, also deactivate all products
        if (!updated.getActive()) {
            List<Product> products = productRepository.findByStudentId(id);
            products.forEach(product -> {
                product.setActive(false);
                productRepository.save(product);
            });
        }

        return convertToDTO(updated);
    }
}
