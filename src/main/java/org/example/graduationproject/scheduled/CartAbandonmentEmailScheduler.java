package org.example.graduationproject.scheduled;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.services.AbandonedCartService;
import org.example.graduationproject.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CartAbandonmentEmailScheduler {

    @Autowired
    private AbandonedCartService abandonedCartService;

    @Autowired
    private EmailService emailService;

    @Value("${cart.abandonment.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${cart.abandonment.email.delay.minutes:1}")
    private int delayMinutes;

    /**
     * Chạy mỗi phút để kiểm tra giỏ hàng cần gửi email thông báo
     * Gửi email cho giỏ hàng active và có sản phẩm sau 1 phút không thanh toán
     */
    @Scheduled(fixedRate = 60000) // Mỗi phút = 60 * 1000 ms
    @Transactional
    public void sendCartAbandonmentEmails() {
        if (!emailEnabled) {
            return;
        }

        try {
            // Tính thời gian cutoff: hiện tại - delayMinutes
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(delayMinutes);
            
            // Lấy danh sách giỏ hàng cần gửi email
            List<GioHang> cartsToNotify = abandonedCartService.getCartsForEmailNotification(cutoffTime);
            
            if (!cartsToNotify.isEmpty()) {
                System.out.println("=== Bắt đầu gửi email thông báo giỏ hàng bỏ dở ===");
                System.out.println("Tìm thấy " + cartsToNotify.size() + " giỏ hàng cần gửi email");
                
                int emailSent = 0;
                for (GioHang cart : cartsToNotify) {
                    try {
                        // Kiểm tra xem giỏ hàng có sản phẩm và user có email không
                        if (cart.getUser() != null && 
                            cart.getUser().getEmail() != null && 
                            !cart.getUser().getEmail().isEmpty() &&
                            cart.getChiTietGioHangs() != null && 
                            !cart.getChiTietGioHangs().isEmpty()) {
                            
                            // Gửi email thông báo
                            emailService.sendCartAbandonmentEmail(cart.getUser(), cart);
                            
                            // Đánh dấu đã gửi email và lưu thời điểm gửi
                            cart.setEmailSent(true);
                            cart.setEmailSentAt(LocalDateTime.now());
                            cart.setNgayCapNhat(LocalDateTime.now());
                            
                            emailSent++;
                            System.out.println("Đã gửi email cho giỏ hàng ID: " + cart.getId() + 
                                             " - User: " + cart.getUser().getEmail());
                        } else {
                            System.out.println("Bỏ qua giỏ hàng ID " + cart.getId() + 
                                             " - User: " + (cart.getUser() != null ? cart.getUser().getEmail() : "null") +
                                             " - Chi tiết: " + (cart.getChiTietGioHangs() != null ? cart.getChiTietGioHangs().size() : "null"));
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi khi gửi email cho giỏ hàng ID " + cart.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // Lưu trạng thái đã gửi email
                abandonedCartService.saveCarts(cartsToNotify);
                
                System.out.println("Đã gửi " + emailSent + " email thông báo giỏ hàng bỏ dở");
                System.out.println("=== Hoàn thành gửi email thông báo ===");
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong CartAbandonmentEmailScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Chạy mỗi 10 phút để dọn dẹp giỏ hàng đã gửi email quá lâu
     * Chuyển giỏ hàng đã gửi email > 10 phút sang abandoned
     */
    @Scheduled(fixedRate = 600000) // 10 phút = 10 * 60 * 1000 ms
    public void cleanupEmailSentCarts() {
        try {
            System.out.println("=== Bắt đầu dọn dẹp giỏ hàng đã gửi email ===");
            int cleanedCount = abandonedCartService.cleanupEmailSentCarts(10); // 10 phút
            System.out.println("Đã dọn dẹp " + cleanedCount + " giỏ hàng đã gửi email");
            System.out.println("=== Hoàn thành dọn dẹp ===");
        } catch (Exception e) {
            System.err.println("Lỗi khi dọn dẹp giỏ hàng đã gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
