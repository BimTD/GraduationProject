package org.example.graduationproject;

import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.services.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserRegistrationTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSuccessfulRegistration() {
        // Test data
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        // Perform registration
        UserRegistrationService.RegistrationResult result = userRegistrationService.registerUser(username, email, password);

        // Assertions
        assertTrue(result.isSuccess(), "Registration should be successful");
        assertEquals("Đăng ký thành công! Vui lòng đăng nhập.", result.getMessage());

        // Verify user was created in database
        Optional<User> createdUser = userRepository.findByUsername(username);
        assertTrue(createdUser.isPresent(), "User should be created in database");
        
        User user = createdUser.get();
        assertEquals(email, user.getEmail());
        assertEquals("1", user.getEnabled());
        assertEquals("local", user.getProvider());
    }

    @Test
    public void testRegistrationWithExistingUsername() {
        // First registration
        userRegistrationService.registerUser("existinguser", "existing@example.com", "password123");

        // Try to register with same username
        UserRegistrationService.RegistrationResult result = userRegistrationService.registerUser("existinguser", "newemail@example.com", "password456");

        // Assertions
        assertFalse(result.isSuccess(), "Registration should fail");
        assertEquals("Tên đăng nhập đã tồn tại!", result.getMessage());
    }

    @Test
    public void testRegistrationWithExistingEmail() {
        // First registration
        userRegistrationService.registerUser("user1", "existing@example.com", "password123");

        // Try to register with same email
        UserRegistrationService.RegistrationResult result = userRegistrationService.registerUser("user2", "existing@example.com", "password456");

        // Assertions
        assertFalse(result.isSuccess(), "Registration should fail");
        assertEquals("Email đã được sử dụng!", result.getMessage());
    }

    @Test
    public void testRegistrationWithShortPassword() {
        // Try to register with short password
        UserRegistrationService.RegistrationResult result = userRegistrationService.registerUser("shortpassuser", "short@example.com", "123");

        // Assertions
        assertFalse(result.isSuccess(), "Registration should fail");
        assertEquals("Mật khẩu phải có ít nhất 6 ký tự!", result.getMessage());
    }

    @Test
    public void testRegistrationWithValidPassword() {
        // Try to register with valid password
        UserRegistrationService.RegistrationResult result = userRegistrationService.registerUser("validpassuser", "valid@example.com", "password123");

        // Assertions
        assertTrue(result.isSuccess(), "Registration should succeed with valid password");
    }
} 