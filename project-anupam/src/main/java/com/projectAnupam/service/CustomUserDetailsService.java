package com.projectAnupam.service;

import com.projectAnupam.entity.Admin;
import com.projectAnupam.entity.Student;
import com.projectAnupam.entity.UserType;
import com.projectAnupam.repository.AdminRepository;
import com.projectAnupam.repository.StudentRepository;
import com.projectAnupam.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            return new CustomUserDetails(
                    admin.getId(),
                    admin.getEmail(),
                    admin.getPassword(),
                    UserType.ADMIN,
                    admin.getActive()
            );
        }

        // Check if student
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(
                student.getId(),
                student.getEmail(),
                student.getPassword(),
                UserType.STUDENT,
                student.getActive()
        );
    }
}