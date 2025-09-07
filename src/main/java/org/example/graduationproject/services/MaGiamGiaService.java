package org.example.graduationproject.services;

import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.models.SanPham;

import java.math.BigDecimal;
import java.util.List;

public interface MaGiamGiaService {
    
    // Tạo mã giảm giá mới
    MaGiamGia createMaGiamGia(MaGiamGia maGiamGia);
    
    // Cập nhật mã giảm giá
    MaGiamGia updateMaGiamGia(MaGiamGia maGiamGia);
    
    // Lấy mã giảm giá theo ID
    MaGiamGia getMaGiamGiaById(Integer id);
    
    // Lấy mã giảm giá theo mã code
    MaGiamGia getMaGiamGiaByCode(String maGiamGia);
    
    // Lấy tất cả mã giảm giá
    List<MaGiamGia> getAllMaGiamGia();
    
    // Lấy mã giảm giá đang hoạt động
    List<MaGiamGia> getActiveMaGiamGia();
    
    // Xóa mã giảm giá
    void deleteMaGiamGia(Integer id);
    
    // Kiểm tra mã giảm giá có hợp lệ không
    boolean isValidMaGiamGia(String maGiamGia);
    
    // Tính giá trị giảm giá cho đơn hàng
    BigDecimal calculateDiscountAmount(String maGiamGia, BigDecimal orderTotal, List<SanPham> products);
    
    // Áp dụng mã giảm giá (tăng số lần sử dụng)
    void applyMaGiamGia(String maGiamGia);
    
    // Kiểm tra mã giảm giá có thể áp dụng cho sản phẩm không
    boolean canApplyToProducts(String maGiamGia, List<SanPham> products);
    
    // Lấy mã giảm giá có thể áp dụng cho sản phẩm
    List<MaGiamGia> getApplicableMaGiamGiaForProducts(List<SanPham> products);
    
    // Cập nhật trạng thái mã giảm giá
    void updateMaGiamGiaStatus(Integer id, String status);
    
    // Tự động cập nhật trạng thái mã giảm giá hết hạn
    void updateExpiredMaGiamGia();
}
