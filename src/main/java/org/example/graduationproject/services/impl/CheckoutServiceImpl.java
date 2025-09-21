package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;
import org.example.graduationproject.exceptions.AuthenticationException;
import org.example.graduationproject.exceptions.ValidationException;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.HoaDonRepository;
import org.example.graduationproject.services.AuthenticationService;
import org.example.graduationproject.services.CheckoutService;
import org.example.graduationproject.services.GioHangService;
import org.example.graduationproject.services.HoaDonService;
import org.example.graduationproject.services.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MaGiamGiaService maGiamGiaService;
    
    @Autowired
    private HoaDonRepository hoaDonRepository;

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

        // Kiểm tra thời gian hết hạn thanh toán
        if (gioHangService.isCartPaymentExpired(cart)) {
            throw new ValidationException("Thời gian thanh toán đã hết hạn. Vui lòng thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm.");
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

        // Kiểm tra thời gian hết hạn thanh toán
        GioHang cart = gioHangService.getActiveCart(user);
        if (gioHangService.isCartPaymentExpired(cart)) {
            throw new ValidationException("Thời gian thanh toán đã hết hạn. Vui lòng thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm.");
        }

        // Tạo đơn hàng
        HoaDon hoaDon = hoaDonService.createOrderFromCart(user, checkoutDTO);
        
        return new CheckoutResponseDTO(true, "Đặt hàng thành công! Mã đơn hàng: " + hoaDon.getId(), hoaDon);
    }

    @Override
    public CheckoutResponseDTO validateMaGiamGia(String maGiamGia) {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        // Validate input
        if (maGiamGia == null || maGiamGia.trim().isEmpty()) {
            throw new ValidationException("Mã giảm giá không được để trống");
        }

        // Lấy giỏ hàng active
        GioHang cart = gioHangService.getActiveCart(user);
        if (cart == null || cart.getChiTietGioHangs() == null || cart.getChiTietGioHangs().isEmpty()) {
            throw new ValidationException("Giỏ hàng trống");
        }

        // Tính tổng tiền giỏ hàng
        java.math.BigDecimal tongTien = cart.getChiTietGioHangs().stream()
                .map(org.example.graduationproject.models.ChiTietGioHang::getThanhTien)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Lấy danh sách sản phẩm
        java.util.List<org.example.graduationproject.models.SanPham> products = cart.getChiTietGioHangs().stream()
            .map(org.example.graduationproject.models.ChiTietGioHang::getSanPhamBienThe)
            .map(org.example.graduationproject.models.SanPhamBienThe::getSanPham)
            .distinct()
            .toList();

        // Tính giá trị giảm giá
        java.math.BigDecimal giaTriGiamGia = maGiamGiaService.calculateDiscountAmount(maGiamGia, tongTien, products);

        if (giaTriGiamGia.compareTo(java.math.BigDecimal.ZERO) > 0) {
            return new CheckoutResponseDTO(true, "Mã giảm giá hợp lệ. Giảm: " + giaTriGiamGia + " VNĐ");
        } else {
            throw new ValidationException("Mã giảm giá không hợp lệ hoặc không áp dụng được cho đơn hàng này");
        }
    }
    
    @Override
    public HoaDon getHoaDonById(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        
        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(orderId);
        return hoaDonOpt.orElse(null);
    }
}





