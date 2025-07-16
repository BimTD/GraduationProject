package org.example.graduationproject.services;

import org.example.graduationproject.models.Role;
import org.example.graduationproject.models.User;
import org.example.graduationproject.models.UserRole;
import org.example.graduationproject.repositories.RoleRepository;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        // Tìm user theo email
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Cập nhật thông tin nếu cần
            if (user.getProvider() == null || !user.getProvider().equals(provider)) {
                user.setProvider(provider);
                userRepository.save(user);
            }
        } else {
            // Tạo user mới
            user = createNewOAuth2User(email, name, provider);
        }
        
        // Tạo authorities từ roles của user
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (UserRole userRole : user.getUserRoles()) {
            authorities.add(new SimpleGrantedAuthority(userRole.getRole().getName()));
        }
        
        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "email" // Sử dụng email làm name attribute
        );
    }
    
    private User createNewOAuth2User(String email, String name, String provider) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email); // Sử dụng email làm username
        user.setProvider(provider);
        user.setEnabled("1");
        user.setPassword(""); // Không cần password cho OAuth2
        
        user = userRepository.save(user);
        
        // Gán role mặc định là USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        
        UserRole userRoleEntity = new UserRole();
        userRoleEntity.setUser(user);
        userRoleEntity.setRole(userRole);
        userRoleRepository.save(userRoleEntity);
        
        return user;
    }
} 