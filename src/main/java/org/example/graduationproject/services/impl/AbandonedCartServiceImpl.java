package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.repositories.GioHangRepository;
import org.example.graduationproject.services.AbandonedCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AbandonedCartServiceImpl implements AbandonedCartService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Override
    @Transactional
    public int updateAbandonedCarts(int cutoffHours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(cutoffHours);
        
        // Chỉ lấy giỏ hàng có sản phẩm để tránh chuyển giỏ hàng trống
        List<GioHang> oldCarts = gioHangRepository.findActiveCartsWithItemsOlderThan(cutoffTime);
        
        int updatedCount = 0;
        for (GioHang cart : oldCarts) {
            cart.setTrangThai("abandoned");
            cart.setNgayCapNhat(LocalDateTime.now());
            updatedCount++;
        }
        
        if (!oldCarts.isEmpty()) {
            gioHangRepository.saveAll(oldCarts);
            System.out.println("Đã chuyển " + updatedCount + " giỏ hàng sang trạng thái abandoned");
        }
        
        return updatedCount;
    }

    @Override
    @Transactional
    public int updateAbandonedCarts() {
        return updateAbandonedCarts(24); // Mặc định 24 giờ
    }

    @Override
    public List<GioHang> getOldActiveCarts(LocalDateTime cutoffTime) {
        return gioHangRepository.findActiveCartsWithItemsOlderThan(cutoffTime);
    }

    @Override
    public Map<String, Object> getAbandonedCartStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalActiveCarts = gioHangRepository.countByTrangThai("active");
        long totalAbandonedCarts = gioHangRepository.countByTrangThai("abandoned");
        long totalOrderedCarts = gioHangRepository.countByTrangThai("ordered");
        
        // Tính giỏ hàng có thể bỏ dở (active > 24h)
        LocalDateTime cutoff24h = LocalDateTime.now().minusHours(24);
        List<GioHang> potentialAbandoned = getOldActiveCarts(cutoff24h);
        
        // Đảm bảo tất cả giá trị đều là Long hoặc Integer
        stats.put("totalActiveCarts", Long.valueOf(totalActiveCarts));
        stats.put("totalAbandonedCarts", Long.valueOf(totalAbandonedCarts));
        stats.put("totalOrderedCarts", Long.valueOf(totalOrderedCarts));
        stats.put("potentialAbandonedCarts", Long.valueOf(potentialAbandoned.size()));
        stats.put("abandonmentRate", totalAbandonedCarts > 0 ? 
            (double) totalAbandonedCarts / (totalAbandonedCarts + totalOrderedCarts) * 100 : 0.0);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GioHang> getCartsForEmailNotification(LocalDateTime cutoffTime) {
        // Lấy giỏ hàng active, có sản phẩm, chưa gửi email, và cũ hơn cutoffTime
        List<GioHang> carts = gioHangRepository.findCartsForEmailNotification(cutoffTime);
        
        // Force load chi tiết giỏ hàng để tránh LazyInitializationException
        for (GioHang cart : carts) {
            if (cart.getChiTietGioHangs() != null) {
                cart.getChiTietGioHangs().size(); // Force load collection
            }
        }
        
        return carts;
    }

    @Override
    @Transactional
    public void saveCarts(List<GioHang> carts) {
        if (!carts.isEmpty()) {
            gioHangRepository.saveAll(carts);
        }
    }

    @Override
    @Transactional
    public int cleanupEmailSentCarts(int cutoffMinutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(cutoffMinutes);
        
        // Lấy giỏ hàng đã gửi email và cũ hơn cutoffTime
        List<GioHang> emailSentCarts = gioHangRepository.findEmailSentCartsOlderThan(cutoffTime);
        
        int cleanedCount = 0;
        for (GioHang cart : emailSentCarts) {
            cart.setTrangThai("abandoned");
            cart.setNgayCapNhat(LocalDateTime.now());
            cleanedCount++;
        }
        
        if (!emailSentCarts.isEmpty()) {
            gioHangRepository.saveAll(emailSentCarts);
            System.out.println("Đã chuyển " + cleanedCount + " giỏ hàng đã gửi email sang trạng thái abandoned");
        }
        
        return cleanedCount;
    }
}
