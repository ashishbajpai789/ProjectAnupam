package com.projectAnupam.global;

import com.projectAnupam.repository.AdminRepository;
import com.projectAnupam.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class Util {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;

    public boolean emailAlreadyInUse(String email){
        return adminRepository.findByEmail(email).isPresent() || studentRepository.findByEmail(email).isPresent();
    }

    public String capitalizeCategories(String input) {
        if (input == null || input.isBlank()) return input;

        // Split by comma, trim spaces, capitalize first letter of each word, join back
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .map(word -> {
                    if (word.isEmpty()) return word;
                    return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
                })
                .collect(Collectors.joining(", "));
    }
}
