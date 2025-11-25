package com.projectAnupam.dto;

import com.projectAnupam.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userType; // ADMIN or STUDENT
    private Long userId;
    private String name;
    private String email;
    private String message;
}
