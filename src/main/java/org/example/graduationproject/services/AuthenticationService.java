package org.example.graduationproject.services;

import org.example.graduationproject.models.User;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     * @return User object hoặc null nếu không authenticated
     */
    User getCurrentUser();
    
    /**
     * Kiểm tra xem user có đang authenticated không
     * @return true nếu authenticated, false nếu không
     */
    boolean isAuthenticated();
    
    /**
     * Lấy username của user hiện tại
     * @return username hoặc null nếu không authenticated
     */
    String getCurrentUsername();
    
    /**
     * Lấy Authentication object hiện tại
     * @return Authentication object hoặc null
     */
    Authentication getCurrentAuthentication();
}

