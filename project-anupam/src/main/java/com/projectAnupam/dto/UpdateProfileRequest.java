package com.projectAnupam.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String currentPassword; // Required if changing password
    private String newPassword;
}
