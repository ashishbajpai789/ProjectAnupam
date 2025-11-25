package com.projectAnupam.service;

import com.projectAnupam.dto.LoginRequest;
import com.projectAnupam.dto.LoginResponse;
import com.projectAnupam.entity.Admin;
import com.projectAnupam.entity.Student;
import com.projectAnupam.entity.UserType;
import com.projectAnupam.exception.UnauthorizedException;
import com.projectAnupam.repository.AdminRepository;
import com.projectAnupam.repository.StudentRepository;
import com.projectAnupam.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtService.generateToken(
                    userDetails.getEmail(),
                    userDetails.getId(),
                    userDetails.getUserType()
            );

            // Save token to database
            jwtService.saveToken(token, userDetails.getId(), userDetails.getUserType());

            // Prepare response
            String name;
            if (userDetails.getUserType() == UserType.ADMIN) {
                System.err.println("Admin user trying to login: "+userDetails.getId());
                Admin admin = adminRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
                name = admin.getName();
            } else {
                System.err.println("Student user trying to login: "+userDetails.getId());
                Student student = studentRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new UsernameNotFoundException("Student not found"));
                name = student.getName();
            }

            return new LoginResponse(
                    token,
                    userDetails.getUserType().name(),
                    userDetails.getId(),
                    name,
                    userDetails.getEmail(),
                    "Login successful"
            );

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Transactional
    public void logout(String token) {
        // Extract Bearer token
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            jwtService.revokeToken(jwtToken);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                String email = jwtService.extractUsername(jwtToken);
                return jwtService.isTokenValid(jwtToken, email);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}

