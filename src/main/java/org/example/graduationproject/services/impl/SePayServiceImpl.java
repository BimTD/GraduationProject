package org.example.graduationproject.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.SePayTransaction;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.SePayTransactionRepository;
import org.example.graduationproject.services.HoaDonService;
import org.example.graduationproject.services.NotificationService;
import org.example.graduationproject.services.SePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SePayServiceImpl implements SePayService {

    @Value("${sepay.webhook.api-key}")
    private String sepayApiKey;

    @Value("${sepay.bank.account:29620036886}")
    private String bankAccount;

    @Value("${sepay.bank.name:TPBank}")
    private String bankName;

    @Value("${sepay.qr.base-url:https://qr.sepay.vn/img}")
    private String qrBaseUrl;

    @Autowired
    private SePayTransactionRepository sePayTransactionRepository;

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private NotificationService notificationService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SePayTransaction createPayment(User user, HoaDon hoaDon, BigDecimal amount, String description) {
        try {
            // Tạo transaction ID duy nhất
            String transactionId = "SEPAY_" + System.currentTimeMillis() + "_" + hoaDon.getId();
            
            // Tạo QR code URL
            String qrCodeUrl = generateQRCodeUrl(bankAccount, bankName, amount, description);
            
            // Tạo SePayTransaction
            SePayTransaction transaction = new SePayTransaction();
            transaction.setTransactionId(transactionId);
            transaction.setOrderId(hoaDon.getId().toString());
            transaction.setAmount(amount);
            transaction.setDescription(description);
            transaction.setBankAccount(bankAccount);
            transaction.setBankName(bankName);
            transaction.setQrCodeUrl(qrCodeUrl);
            transaction.setStatus("PENDING");
            transaction.setUser(user);
            transaction.setHoaDon(hoaDon);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());
            
            // Lưu vào database
            SePayTransaction savedTransaction = sePayTransactionRepository.save(transaction);
            
            // Tạo thông báo cho user
            String title = "QR Code thanh toán đã được tạo";
            String message = "Vui lòng quét QR code để thanh toán đơn hàng #" + hoaDon.getId() + 
                           " với số tiền " + formatCurrency(amount);
            notificationService.createNotification(title, message, "PAYMENT_QR_CREATED", user, hoaDon.getId());
            
            return savedTransaction;
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo giao dịch SePay: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean processWebhook(Map<String, Object> webhookData) {
        try {
            System.out.println("Processing webhook data: " + webhookData);
            
            // Lấy các field từ SePay webhook format
            String transactionId = getStringValue(webhookData, "id", "transaction_id", "transactionId");
            String transferType = getStringValue(webhookData, "transferType", "status", "state", "payment_status");
            String content = getStringValue(webhookData, "content", "description", "order_id", "orderId");
            String amount = getStringValue(webhookData, "transferAmount", "amount");
            String referenceCode = getStringValue(webhookData, "referenceCode", "reference_code");
            
            // Map transferType to status
            String status = mapTransferTypeToStatus(transferType);
            
            System.out.println("Extracted - transactionId: " + transactionId + ", status: " + status + ", content: " + content);
            System.out.println("Amount: " + amount + ", ReferenceCode: " + referenceCode);
            
            if (transactionId == null || status == null) {
                System.out.println("Missing required fields: transactionId=" + transactionId + ", status=" + status);
                return false;
            }
            
            // Tìm giao dịch
            Optional<SePayTransaction> transactionOpt = sePayTransactionRepository.findByTransactionId(transactionId);
            if (!transactionOpt.isPresent()) {
                System.out.println("Transaction not found with ID: " + transactionId);
                // Tìm theo orderId nếu có
                String orderId = extractOrderIdFromContent(content);
                if (orderId != null) {
                    transactionOpt = sePayTransactionRepository.findByOrderId(orderId);
                    if (transactionOpt.isPresent()) {
                        System.out.println("Found transaction by orderId: " + orderId);
                    }
                }
                
                if (!transactionOpt.isPresent()) {
                    System.out.println("No transaction found for transactionId: " + transactionId + " or orderId: " + orderId);
                    // Tạo transaction mới nếu không tìm thấy
                    if (orderId != null) {
                        System.out.println("Creating new transaction for orderId: " + orderId);
                        try {
                            Integer orderIdInt = Integer.parseInt(orderId);
                            // Tìm hóa đơn
                            HoaDon hoaDon = hoaDonService.getOrderById(orderIdInt);
                            if (hoaDon != null) {
                                // Tạo transaction mới
                                SePayTransaction newTransaction = new SePayTransaction();
                                newTransaction.setTransactionId(transactionId);
                                newTransaction.setOrderId(orderId);
                                newTransaction.setAmount(new BigDecimal(amount != null ? amount : "0"));
                                newTransaction.setDescription(content);
                                newTransaction.setBankAccount("29620036886");
                                newTransaction.setBankName("TPBank");
                                newTransaction.setStatus(status);
                                newTransaction.setWebhookData(objectMapper.writeValueAsString(webhookData));
                                newTransaction.setUser(hoaDon.getUser());
                                newTransaction.setHoaDon(hoaDon);
                                newTransaction.setCreatedAt(LocalDateTime.now());
                                newTransaction.setUpdatedAt(LocalDateTime.now());
                                
                                SePayTransaction newSavedTransaction = sePayTransactionRepository.save(newTransaction);
                                System.out.println("Created new transaction: " + newSavedTransaction.getId());
                                transactionOpt = Optional.of(newSavedTransaction);
                            } else {
                                System.out.println("HoaDon not found for orderId: " + orderIdInt);
                                return false;
                            }
                        } catch (Exception e) {
                            System.err.println("Error creating new transaction: " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            
            SePayTransaction transaction = transactionOpt.get();
            System.out.println("Found transaction: " + transaction.getId() + ", current status: " + transaction.getStatus());
            
            // Cập nhật trạng thái
            transaction.setStatus(status);
            transaction.setWebhookData(objectMapper.writeValueAsString(webhookData));
            transaction.setUpdatedAt(LocalDateTime.now());
            System.out.println("Updated transaction status to: " + status);
            
            if ("SUCCESS".equals(status)) {
                transaction.setCompletedAt(LocalDateTime.now());
                
            // Cập nhật trạng thái đơn hàng
            String orderId = extractOrderIdFromContent(content);
            System.out.println("Extracted orderId from content: " + orderId);
            if (orderId != null) {
                try {
                    Integer orderIdInt = Integer.parseInt(orderId);
                    System.out.println("Updating order status for orderId: " + orderIdInt);
                    hoaDonService.updateOrderStatus(orderIdInt, "CONFIRMED");
                    System.out.println("Order status updated successfully");
                    
                    // Tạo thông báo thành công
                    String title = "Thanh toán thành công";
                    String message = "Đơn hàng #" + orderId + " đã được thanh toán thành công qua SePay";
                    notificationService.createNotification(
                        title, 
                        message, 
                        "PAYMENT_SUCCESS", 
                        transaction.getUser(), 
                        orderIdInt
                    );
                    System.out.println("Notification sent successfully");
                    
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("No orderId extracted from content: " + content);
            }
            } else if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                // Tạo thông báo thất bại
                String orderId = extractOrderIdFromContent(content);
                String title = "Thanh toán thất bại";
                String message = "Giao dịch thanh toán cho đơn hàng #" + (orderId != null ? orderId : "không xác định") + " đã thất bại";
                notificationService.createNotification(
                    title, 
                    message, 
                    "PAYMENT_FAILED", 
                    transaction.getUser(), 
                    orderId != null ? Integer.parseInt(orderId) : null
                );
            }
            
            sePayTransactionRepository.save(transaction);
            System.out.println("Transaction saved successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý webhook SePay: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SePayTransaction checkTransactionStatus(String transactionId) {
        return sePayTransactionRepository.findByTransactionId(transactionId).orElse(null);
    }

    @Override
    public String generateQRCodeUrl(String bankAccount, String bankName, BigDecimal amount, String description) {
        try {
            StringBuilder url = new StringBuilder(qrBaseUrl);
            url.append("?acc=").append(bankAccount);
            url.append("&bank=").append(bankName);
            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                url.append("&amount=").append(amount.toString());
            }
            if (description != null && !description.trim().isEmpty()) {
                url.append("&des=").append(java.net.URLEncoder.encode(description, StandardCharsets.UTF_8));
            }
            url.append("&template=compact");
            
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo QR code URL: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String signature, String payload) {
        try {
            // Tạo signature từ payload và API key
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(sepayApiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);
            
            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            System.err.println("Lỗi khi xác thực webhook signature: " + e.getMessage());
            return false;
        }
    }

    @Override
    public SePayTransaction updateTransactionStatus(String transactionId, String status, Map<String, Object> webhookData) {
        Optional<SePayTransaction> transactionOpt = sePayTransactionRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            SePayTransaction transaction = transactionOpt.get();
            transaction.setStatus(status);
            transaction.setUpdatedAt(LocalDateTime.now());
            
            try {
                transaction.setWebhookData(objectMapper.writeValueAsString(webhookData));
            } catch (Exception e) {
                System.err.println("Lỗi khi lưu webhook data: " + e.getMessage());
            }
            
            if ("SUCCESS".equals(status)) {
                transaction.setCompletedAt(LocalDateTime.now());
            }
            
            return sePayTransactionRepository.save(transaction);
        }
        return null;
    }

    @Override
    public List<SePayTransaction> getUserTransactions(Long userId) {
        return sePayTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public boolean cancelTransaction(String transactionId) {
        Optional<SePayTransaction> transactionOpt = sePayTransactionRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            SePayTransaction transaction = transactionOpt.get();
            if ("PENDING".equals(transaction.getStatus())) {
                transaction.setStatus("CANCELLED");
                transaction.setUpdatedAt(LocalDateTime.now());
                sePayTransactionRepository.save(transaction);
                return true;
            }
        }
        return false;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", amount);
    }
    
    /**
     * Helper method để lấy string value từ map với nhiều key có thể
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }
    
    /**
     * Map SePay transferType sang status
     */
    private String mapTransferTypeToStatus(String transferType) {
        if (transferType == null) return null;
        
        switch (transferType.toLowerCase()) {
            case "in":
                return "SUCCESS";
            case "out":
                return "FAILED";
            default:
                return "PENDING";
        }
    }
    
    /**
     * Extract order ID từ content string
     * Ví dụ: "Thanh toan don hang 113-160925-13:28:53 305238" -> "113"
     */
    private String extractOrderIdFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        // Tìm pattern "don hang XXX-" hoặc "don hang XXX "
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("don hang (\\d+)[-\\s]");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Fallback: tìm số đầu tiên trong content
        pattern = java.util.regex.Pattern.compile("(\\d+)");
        matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
}
