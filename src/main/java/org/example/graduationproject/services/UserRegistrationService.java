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
            return new RegistrationResult(false, "Tên người dùng đã tồn tại!");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return new RegistrationResult(false, "Email đã được sử dụng!");
        }

        if (password.length() < 6) {
            return new RegistrationResult(false, "Mật khẩu phải có ít nhất 6 ký tự!");
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
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò NGƯỜI DÙNG"));

            UserRole userRoleEntity = new UserRole();
            userRoleEntity.setUser(newUser);
            userRoleEntity.setRole(userRole);
            userRoleRepository.save(userRoleEntity);

            return new RegistrationResult(true, "Đăng ký thành công! Vui lòng đăng nhập.");
        } catch (Exception e) {
            return new RegistrationResult(false, "Đã xảy ra lỗi khi đăng ký: " + e.getMessage());
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