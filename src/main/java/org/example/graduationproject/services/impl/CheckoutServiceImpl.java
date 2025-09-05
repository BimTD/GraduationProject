package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;
import org.example.graduationproject.exceptions.AuthenticationException;
import org.example.graduationproject.exceptions.ValidationException;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.AuthenticationService;
import org.example.graduationproject.services.CheckoutService;
import org.example.graduationproject.services.GioHangService;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public CheckoutResponseDTO getCheckoutPageDataWithValidation() {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        // Lấy giỏ hàng active
        GioHang cart = gioHangService.getActiveCart(user);
        if (cart == null || cart.getChiTietGioHangs() == null || cart.getChiTietGioHangs().isEmpty()) {
            throw new ValidationException("Giỏ hàng trống");
        }

        return new CheckoutResponseDTO(true, "Lấy dữ liệu checkout thành công", cart, user);
    }

    @Override
    public CheckoutResponseDTO processCheckoutWithValidation(CheckoutDTO checkoutDTO) {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        // Validate input
        if (checkoutDTO == null) {
            throw new ValidationException("Dữ liệu checkout không hợp lệ");
        }

        if (checkoutDTO.getTenNguoiNhan() == null || checkoutDTO.getTenNguoiNhan().trim().isEmpty()) {
            throw new ValidationException("Tên người nhận không được để trống");
        }

        if (checkoutDTO.getSoDienThoai() == null || checkoutDTO.getSoDienThoai().trim().isEmpty()) {
            throw new ValidationException("Số điện thoại không được để trống");
        }

        if (checkoutDTO.getDiaChiGiaoHang() == null || checkoutDTO.getDiaChiGiaoHang().trim().isEmpty()) {
            throw new ValidationException("Địa chỉ giao hàng không được để trống");
        }

        // Tạo đơn hàng
        HoaDon hoaDon = hoaDonService.createOrderFromCart(user, checkoutDTO);
        
        return new CheckoutResponseDTO(true, "Đặt hàng thành công! Mã đơn hàng: " + hoaDon.getId(), hoaDon);
    }
}


