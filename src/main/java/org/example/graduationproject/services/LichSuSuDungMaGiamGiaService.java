package org.example.graduationproject.services;

import org.example.graduationproject.models.LichSuSuDungMaGiamGia;
import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.models.User;

import java.math.BigDecimal;
import java.util.List;

public interface LichSuSuDungMaGiamGiaService {
    
    /**
     * Kiểm tra xem user đã sử dụng mã giảm giá này chưa
     */
    boolean hasUserUsedMaGiamGia(User user, String maGiamGiaCode);
    
    /**
     * Lưu lịch sử sử dụng mã giảm giá
     */
    LichSuSuDungMaGiamGia saveUsageHistory(User user, MaGiamGia maGiamGia, BigDecimal giaTriGiamGia, Long donHangId);
    
    /**
     * Lấy danh sách mã giảm giá đã sử dụng bởi user
     */
    List<String> getUsedMaGiamGiaCodesByUser(User user);
    
    /**
     * Lấy lịch sử sử dụng mã giảm giá của user
     */
    List<LichSuSuDungMaGiamGia> getUsageHistoryByUser(User user);
    
    /**
     * Lưu lịch sử sử dụng nhiều mã giảm giá cho một đơn hàng
     */
    List<LichSuSuDungMaGiamGia> saveMultipleUsageHistory(User user, List<MaGiamGia> maGiamGiaList, List<BigDecimal> giaTriGiamGiaList, Long donHangId);
}

