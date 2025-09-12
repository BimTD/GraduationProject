package org.example.graduationproject.services;

import org.example.graduationproject.models.GioHang;

import java.time.LocalDateTime;
import java.util.List;

public interface AbandonedCartService {
    
    /**
     * Cập nhật giỏ hàng bỏ dở dựa trên thời gian cutoff
     * @param cutoffHours Số giờ để xác định giỏ hàng bỏ dở (mặc định 24h)
     * @return Số lượng giỏ hàng đã được chuyển sang abandoned
     */
    int updateAbandonedCarts(int cutoffHours);
    
    /**
     * Cập nhật giỏ hàng bỏ dở với thời gian mặc định 24 giờ
     * @return Số lượng giỏ hàng đã được chuyển sang abandoned
     */
    int updateAbandonedCarts();
    
    /**
     * Lấy danh sách giỏ hàng active cũ hơn thời gian cutoff
     * @param cutoffTime Thời gian cutoff
     * @return Danh sách giỏ hàng
     */
    List<GioHang> getOldActiveCarts(LocalDateTime cutoffTime);
    
    /**
     * Lấy thống kê giỏ hàng bỏ dở
     * @return Map chứa thống kê
     */
    java.util.Map<String, Object> getAbandonedCartStats();
}
