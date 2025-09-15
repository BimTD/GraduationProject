package org.example.graduationproject.scheduled;

import org.example.graduationproject.services.AbandonedCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AbandonedCartScheduler {

    @Autowired
    private AbandonedCartService abandonedCartService;

    /**
     * Chạy mỗi 2 giờ để cập nhật giỏ hàng bỏ dở
     * Chỉ chuyển giỏ hàng có sản phẩm và không hoạt động > 24h
     */
    @Scheduled(fixedRate = 7200000) // 2 giờ = 2 * 60 * 60 * 1000 ms
    public void updateAbandonedCarts() {
        try {
            int updatedCount = abandonedCartService.updateAbandonedCarts(24);
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật giỏ hàng bỏ dở: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Chạy mỗi ngày lúc 2:00 AM để dọn dẹp giỏ hàng cũ
     * Chuyển giỏ hàng không hoạt động > 7 ngày sang abandoned
     */
    @Scheduled(cron = "0 0 2 * * ?") // Mỗi ngày lúc 2:00 AM
    public void cleanupOldCarts() {
        try {
            System.out.println("=== Bắt đầu dọn dẹp giỏ hàng cũ ===");
            int updatedCount = abandonedCartService.updateAbandonedCarts(168); // 7 ngày = 168 giờ
            System.out.println("Đã dọn dẹp " + updatedCount + " giỏ hàng cũ");
            System.out.println("=== Hoàn thành dọn dẹp giỏ hàng cũ ===");
        } catch (Exception e) {
            System.err.println("Lỗi khi dọn dẹp giỏ hàng cũ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Chạy mỗi 6 giờ để báo cáo thống kê
     */
    @Scheduled(fixedRate = 21600000) // 6 giờ = 6 * 60 * 60 * 1000 ms
    public void reportCartStats() {
        try {
            var stats = abandonedCartService.getAbandonedCartStats();
        } catch (Exception e) {
            System.err.println("Lỗi khi báo cáo thống kê: " + e.getMessage());
        }
    }
}
