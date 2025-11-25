package com.projectAnupam.service;

import com.projectAnupam.dto.AdminDTO;
import com.projectAnupam.dto.UpdateProfileRequest;
import com.projectAnupam.entity.Admin;
import com.projectAnupam.exception.BadRequestException;
import com.projectAnupam.exception.ResourceNotFoundException;
import com.projectAnupam.global.Util;
import com.projectAnupam.repository.AdminRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final Util util;

    @Transactional
    public AdminDTO createAdmin(AdminDTO dto) {
        // Check if email already exists
        if (util.emailAlreadyInUse(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        Admin admin = new Admin();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setActive(true);

        Admin saved = adminRepository.save(admin);
        return convertToDTO(saved);
    }

    public List<AdminDTO> getAllAdminsIncludingInactive() {
        return adminRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminDTO toggleActive(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        // Prevent deactivating the last active admin
        if (admin.getActive()) {
            long activeCount = adminRepository.findAll().stream()
                    .filter(Admin::getActive)
                    .count();
            if (activeCount <= 1) {
                throw new BadRequestException("Cannot deactivate the last active admin");
            }
        }

        admin.setActive(!admin.getActive());
        Admin updated = adminRepository.save(admin);
        return convertToDTO(updated);
    }

    public AdminDTO getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return convertToDTO(admin);
    }

    @Transactional
    public AdminDTO updateProfile(Long adminId, UpdateProfileRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            admin.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!admin.getEmail().equals(request.getEmail()) &&
                    util.emailAlreadyInUse(request.getEmail())) {
                throw new BadRequestException("Email already registered");
            }
            admin.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to change password");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }

            admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        Admin updated = adminRepository.save(admin);
        return convertToDTO(updated);
    }

    private AdminDTO convertToDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setId(admin.getId());
        dto.setName(admin.getName());
        dto.setEmail(admin.getEmail());
        dto.setActive(admin.getActive());
        dto.setCreatedAt(admin.getCreatedAt());
        return dto;
    }
}

