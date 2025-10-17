package org.example.graduationproject.services;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;

public interface EmailService {
    
    /**
     * Gửi email thông báo giỏ hàng bỏ dở
     * @param user Người dùng
     * @param cart Giỏ hàng
     */
    void sendCartAbandonmentEmail(User user, GioHang cart);
    
    /**
     * Gửi email đơn giản
     * @param to Email người nhận
     * @param subject Tiêu đề
     * @param content Nội dung
     */
    void sendSimpleEmail(String to, String subject, String content);
    
    /**
     * Gửi email HTML
     * @param to Email người nhận
     * @param subject Tiêu đề
     * @param htmlContent Nội dung HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
}




































