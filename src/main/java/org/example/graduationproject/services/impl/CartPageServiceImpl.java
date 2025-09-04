package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.CartPageResponseDTO;
import org.example.graduationproject.exceptions.AuthenticationException;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.AuthenticationService;
import org.example.graduationproject.services.CartPageService;
import org.example.graduationproject.services.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartPageServiceImpl implements CartPageService {

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public CartPageResponseDTO getCartPageDataWithValidation() {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        GioHang cart = gioHangService.getActiveCart(user);
        return new CartPageResponseDTO(true, "Lấy dữ liệu giỏ hàng thành công", cart, user);
    }
}
