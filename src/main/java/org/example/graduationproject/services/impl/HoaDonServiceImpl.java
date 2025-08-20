package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.ChiTietHoaDonRepository;
import org.example.graduationproject.repositories.HoaDonRepository;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.services.GioHangService;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @Override
    @Transactional
    public HoaDon createOrderFromCart(User user, CheckoutDTO checkoutDTO) {
        // Lấy giỏ hàng active của user
        GioHang activeCart = gioHangService.getActiveCart(user);
        if (activeCart == null || activeCart.getChiTietGioHangs() == null || activeCart.getChiTietGioHangs().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng");
        }

        // Tạo hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setUser(user);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setGhiChu(checkoutDTO.getGhiChu());
        hoaDon.setTrangThai("PENDING"); // PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
        hoaDon.setLoaiThanhToan(checkoutDTO.getLoaiThanhToan());
        hoaDon.setDaLayTien("NO"); // NO, YES
        
        // Tính tổng tiền
        BigDecimal tongTien = activeCart.getChiTietGioHangs().stream()
                .map(ChiTietGioHang::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Cộng phí giao hàng
        if (checkoutDTO.getPhiGiaoHang() != null) {
            tongTien = tongTien.add(checkoutDTO.getPhiGiaoHang());
        }
        
        hoaDon.setTongTien(tongTien);
        
        // Lưu hóa đơn
        hoaDon = hoaDonRepository.save(hoaDon);

        // Tạo chi tiết hóa đơn
        List<ChiTietHoaDon> chiTietHoaDons = new ArrayList<>();
        for (ChiTietGioHang cartItem : activeCart.getChiTietGioHangs()) {
            ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
            chiTietHoaDon.setHoaDon(hoaDon);
            chiTietHoaDon.setSanPhamBienThe(cartItem.getSanPhamBienThe());
            chiTietHoaDon.setSoLuong(cartItem.getSoLuong());
            chiTietHoaDon.setThanhTien(cartItem.getThanhTien());
            
            chiTietHoaDons.add(chiTietHoaDon);
        }
        
        // Lưu chi tiết hóa đơn
        chiTietHoaDonRepository.saveAll(chiTietHoaDons);
        
        // Cập nhật trạng thái giỏ hàng thành "ordered"
        activeCart.setTrangThai("ordered");
        activeCart.setNgayCapNhat(LocalDateTime.now());
        gioHangService.updateCartStatus(activeCart);

        return hoaDon;
    }

    @Override
    public List<HoaDon> getUserOrders(User user) {
        return hoaDonRepository.findByUserOrderByNgayTaoDesc(user);
    }

    @Override
    public HoaDon getOrderById(Integer orderId) {
        return hoaDonRepository.findById(orderId).orElse(null);
    }

    @Override
    public HoaDon getUserOrderById(User user, Integer orderId) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId).orElse(null);
        if (hoaDon != null && hoaDon.getUser().getId().equals(user.getId())) {
            return hoaDon;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Integer orderId, String newStatus) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId).orElse(null);
        if (hoaDon != null) {
            String currentStatus = hoaDon.getTrangThai();

            // Nếu chuyển từ PENDING -> CONFIRMED thì kiểm tra và trừ tồn kho
            if ("PENDING".equalsIgnoreCase(currentStatus) && "CONFIRMED".equalsIgnoreCase(newStatus)) {
                List<ChiTietHoaDon> chiTietList = chiTietHoaDonRepository.findByHoaDon(hoaDon);

                // Gom số lượng theo từng biến thể để kiểm tra 1 lần
                Map<Integer, Integer> variantIdToRequiredQty = new HashMap<>();
                for (ChiTietHoaDon cthd : chiTietList) {
                    if (cthd.getSanPhamBienThe() == null) continue;
                    Integer variantId = cthd.getSanPhamBienThe().getId();
                    if (variantId == null) continue;
                    int required = cthd.getSoLuong() != null ? cthd.getSoLuong() : 0;
                    variantIdToRequiredQty.merge(variantId, required, Integer::sum);
                }

                // Kiểm tra tồn kho đủ
                List<org.example.graduationproject.models.SanPhamBienThe> toUpdate = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : variantIdToRequiredQty.entrySet()) {
                    org.example.graduationproject.models.SanPhamBienThe variant = sanPhamBienTheRepository.findById(entry.getKey()).orElse(null);
                    if (variant == null) {
                        return false; // biến thể không tồn tại
                    }
                    int stock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
                    int need = entry.getValue() != null ? entry.getValue() : 0;
                    if (stock < need) {
                        return false; // không đủ hàng
                    }
                    variant.setSoLuongTon(stock - need);
                    toUpdate.add(variant);
                }

                // Cập nhật tồn kho
                if (!toUpdate.isEmpty()) {
                    sanPhamBienTheRepository.saveAll(toUpdate);
                }
            }

            hoaDon.setTrangThai(newStatus);
            hoaDonRepository.save(hoaDon);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelOrder(User user, Integer orderId) {
        HoaDon hoaDon = getUserOrderById(user, orderId);
        if (hoaDon != null && "PENDING".equals(hoaDon.getTrangThai())) {
            hoaDon.setTrangThai("CANCELLED");
            hoaDonRepository.save(hoaDon);
            return true;
        }
        return false;
    }
}
