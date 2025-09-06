package org.example.graduationproject.services;

import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UpdateResult updateProfile(Long userId, String hoTen, String email, String soDienThoai, String diaChi) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return new UpdateResult(false, "Không tìm thấy người dùng!");
            }

            User user = userOpt.get();

            // Kiểm tra email có trùng với user khác không
            if (!user.getEmail().equals(email)) {
                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    return new UpdateResult(false, "Email này đã được sử dụng bởi tài khoản khác!");
                }
            }

            // Cập nhật thông tin
            user.setHoTen(hoTen);
            user.setEmail(email);
            user.setSoDienThoai(soDienThoai);
            user.setDiaChi(diaChi);

            userRepository.save(user);
            return new UpdateResult(true, "Cập nhật thông tin thành công!");

        } catch (Exception e) {
            return new UpdateResult(false, "Có lỗi xảy ra khi cập nhật thông tin!");
        }
    }

    public PasswordChangeResult changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        try {
            // Kiểm tra mật khẩu mới và xác nhận mật khẩu
            if (!newPassword.equals(confirmPassword)) {
                return new PasswordChangeResult(false, "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            }

            // Kiểm tra độ dài mật khẩu
            if (newPassword.length() < 6) {
                return new PasswordChangeResult(false, "Mật khẩu mới phải có ít nhất 6 ký tự!");
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return new PasswordChangeResult(false, "Không tìm thấy người dùng!");
            }

            User user = userOpt.get();

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return new PasswordChangeResult(false, "Mật khẩu hiện tại không đúng!");
            }

            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return new PasswordChangeResult(true, "Đổi mật khẩu thành công!");

        } catch (Exception e) {
            return new PasswordChangeResult(false, "Có lỗi xảy ra khi đổi mật khẩu!");
        }
    }

    public static class UpdateResult {
        private final boolean success;
        private final String message;

        public UpdateResult(boolean success, String message) {
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

    public static class PasswordChangeResult {
        private final boolean success;
        private final String message;

        public PasswordChangeResult(boolean success, String message) {
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
