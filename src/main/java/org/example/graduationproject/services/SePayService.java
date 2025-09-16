package org.example.graduationproject.services;

import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.SePayTransaction;
import org.example.graduationproject.models.User;

import java.math.BigDecimal;
import java.util.Map;

public interface SePayService {
    
    /**
     * Tạo giao dịch SePay và QR code
     */
    SePayTransaction createPayment(User user, HoaDon hoaDon, BigDecimal amount, String description);
    
    /**
     * Xử lý webhook callback từ SePay
     */
    boolean processWebhook(Map<String, Object> webhookData);
    
    /**
     * Kiểm tra trạng thái giao dịch
     */
    SePayTransaction checkTransactionStatus(String transactionId);
    
    /**
     * Tạo QR code URL
     */
    String generateQRCodeUrl(String bankAccount, String bankName, BigDecimal amount, String description);
    
    /**
     * Xác thực webhook signature
     */
    boolean verifyWebhookSignature(String signature, String payload);
    
    /**
     * Cập nhật trạng thái giao dịch
     */
    SePayTransaction updateTransactionStatus(String transactionId, String status, Map<String, Object> webhookData);
    
    /**
     * Lấy lịch sử giao dịch của user
     */
    java.util.List<SePayTransaction> getUserTransactions(Long userId);
    
    /**
     * Hủy giao dịch
     */
    boolean cancelTransaction(String transactionId);
}
