package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.ChiTietGioHang;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.EmailService;
import org.example.graduationproject.services.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Value("${cart.abandonment.email.from}")
    private String fromEmail;

    @Value("${cart.abandonment.email.subject}")
    private String defaultSubject;

    @Override
    public void sendCartAbandonmentEmail(User user, GioHang cart) {
        try {
            String subject = defaultSubject;
            String htmlContent = buildCartAbandonmentEmailContent(user, cart);
            
            sendHtmlEmail(user.getEmail(), subject, htmlContent);
            
            System.out.println("Đã gửi email thông báo giỏ hàng bỏ dở cho: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email thông báo giỏ hàng bỏ dở: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            
            mailSender.send(message);
            System.out.println("Đã gửi email đơn giản đến: " + to);
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email đơn giản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("Đã gửi email HTML đến: " + to);
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email HTML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildCartAbandonmentEmailContent(User user, GioHang cart) {
        Context context = new Context();
        
        // Thông tin người dùng
        context.setVariable("userName", user.getHoTen() != null ? user.getHoTen() : user.getUsername());
        context.setVariable("userEmail", user.getEmail());
        
        // Thông tin giỏ hàng
        context.setVariable("cartId", cart.getId());
        context.setVariable("cartCreatedDate", cart.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        // Tính tổng tiền và số lượng sản phẩm
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;
        
        if (cart.getChiTietGioHangs() != null) {
            for (ChiTietGioHang item : cart.getChiTietGioHangs()) {
                totalAmount = totalAmount.add(item.getThanhTien());
                totalItems += item.getSoLuong();
            }
        }
        
        context.setVariable("totalAmount", totalAmount);
        context.setVariable("totalItems", totalItems);
        context.setVariable("cartItems", cart.getChiTietGioHangs() != null ? cart.getChiTietGioHangs() : new java.util.ArrayList<>());
        
        // Lấy mã giảm giá có thể sử dụng cho user
        List<MaGiamGia> availableDiscountCodes = maGiamGiaService.getAvailableMaGiamGiaForUser(user);
        context.setVariable("discountCodes", availableDiscountCodes);
        
        // URL trang web
        context.setVariable("websiteUrl", "http://localhost:8080");
        context.setVariable("cartUrl", "http://localhost:8080/cart");
        context.setVariable("shopUrl", "http://localhost:8080/shop");
        context.setVariable("discountCodesUrl", "http://localhost:8080/discount-codes");
        
        return templateEngine.process("email/simple-cart-abandonment", context);
    }
}
