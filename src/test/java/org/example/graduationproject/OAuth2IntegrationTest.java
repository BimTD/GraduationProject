package org.example.graduationproject;

import org.example.graduationproject.models.Role;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.RoleRepository;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.repositories.UserRoleRepository;
import org.example.graduationproject.services.CustomOAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OAuth2IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomOAuth2UserService oAuth2UserService;

    @Test
    public void testRoleInitialization() {
        // Kiểm tra roles đã được tạo
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");

        assertTrue(userRole.isPresent(), "ROLE_USER should exist");
        assertTrue(adminRole.isPresent(), "ROLE_ADMIN should exist");
    }

    @Test
    public void testAdminUserCreation() {
        // Kiểm tra admin user đã được tạo
        Optional<User> adminUser = userRepository.findByUsername("admin");
        assertTrue(adminUser.isPresent(), "Admin user should exist");

        User admin = adminUser.get();
        assertEquals("admin@example.com", admin.getEmail());
        assertEquals("1", admin.getEnabled());
        assertEquals("local", admin.getProvider());
    }

    @Test
    public void testUserRepositoryFindByEmail() {
        // Test tìm user theo email
        Optional<User> adminUser = userRepository.findByEmail("admin@example.com");
        assertTrue(adminUser.isPresent(), "Should find user by email");
        assertEquals("admin", adminUser.get().getUsername());
    }

    @Test
    public void testPasswordEncoder() {
        // Test password encoder
        String rawPassword = "test123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    public void testOAuth2UserServiceExists() {
        // Kiểm tra OAuth2UserService đã được inject
        assertNotNull(oAuth2UserService, "OAuth2UserService should be injected");
    }
} 