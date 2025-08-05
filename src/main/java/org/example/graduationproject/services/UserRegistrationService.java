package org.example.graduationproject.services;

import org.example.graduationproject.models.Role;
import org.example.graduationproject.models.User;
import org.example.graduationproject.models.UserRole;
import org.example.graduationproject.repositories.RoleRepository;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegistrationResult registerUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return new RegistrationResult(false, "Username already exists!");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return new RegistrationResult(false, "Email already in use!");
        }

        if (password.length() < 6) {
            return new RegistrationResult(false, "Password must be at least 6 characters!");
        }

        try {
            // Tạo user mới
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setEnabled("1");
            newUser.setProvider("local");

            newUser = userRepository.save(newUser);

            // Gán role USER mặc định
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));

            UserRole userRoleEntity = new UserRole();
            userRoleEntity.setUser(newUser);
            userRoleEntity.setRole(userRole);
            userRoleRepository.save(userRoleEntity);

            return new RegistrationResult(true, "Registration successful! Please login.");
        } catch (Exception e) {
            return new RegistrationResult(false, "An error occurred while registering: " + e.getMessage());
        }
    }

    public static class RegistrationResult {
        private final boolean success;
        private final String message;

        public RegistrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
} 