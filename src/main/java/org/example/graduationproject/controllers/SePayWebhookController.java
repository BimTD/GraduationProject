package org.example.graduationproject.controllers;

import org.example.graduationproject.services.SePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {

    @Autowired
    private SePayService sePayService;

    /**
     * Webhook endpoint để nhận callback từ SePay
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody(required = false) Map<String, Object> webhookData,
            @RequestHeader(value = "X-SePay-Signature", required = false) String signature,
            @RequestHeader(value = "Content-Type", required = false) String contentType) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Log webhook data để debug
            System.out.println("=== SEPAY WEBHOOK RECEIVED ===");
            System.out.println("URL: /api/sepay/webhook");
            System.out.println("Content-Type: " + contentType);
            System.out.println("Data: " + webhookData);
            System.out.println("Signature: " + signature);
            System.out.println("=============================");
            
            // Kiểm tra dữ liệu đầu vào
            if (webhookData == null || webhookData.isEmpty()) {
                System.out.println("Warning: Empty webhook data received");
                response.put("success", false);
                response.put("message", "Empty webhook data");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Xác thực signature nếu có
            if (signature != null && !signature.trim().isEmpty()) {
                // Convert webhookData to JSON string for signature verification
                String payload = webhookData.toString();
                
                if (!sePayService.verifyWebhookSignature(signature, payload)) {
                    System.out.println("Warning: Invalid signature");
                    response.put("success", false);
                    response.put("message", "Invalid signature");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }
            
            // Xử lý webhook
            boolean processed = sePayService.processWebhook(webhookData);
            
            if (processed) {
                System.out.println("Webhook processed successfully");
                response.put("success", true);
                response.put("message", "Webhook processed successfully");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Failed to process webhook");
                response.put("success", false);
                response.put("message", "Failed to process webhook");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing SePay webhook: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Test endpoint để kiểm tra webhook
     */
    @GetMapping("/webhook/test")
    public ResponseEntity<Map<String, Object>> testWebhook() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Webhook endpoint is working!");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint với dữ liệu mẫu
     */
    @PostMapping("/webhook/test")
    public ResponseEntity<Map<String, Object>> testWebhookWithData(@RequestBody(required = false) Map<String, Object> testData) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("=== TEST WEBHOOK RECEIVED ===");
        System.out.println("Test Data: " + testData);
        System.out.println("=============================");
        
        response.put("success", true);
        response.put("message", "Test webhook processed successfully!");
        response.put("received_data", testData);
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * API để tạo giao dịch thanh toán SePay
     */
    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> paymentData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Lấy thông tin từ request
            Integer orderId = (Integer) paymentData.get("orderId");
            String description = (String) paymentData.get("description");
            
            if (orderId == null) {
                response.put("success", false);
                response.put("message", "Order ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Tạo giao dịch (cần implement logic lấy user và hóa đơn)
            // SePayTransaction transaction = sePayService.createPayment(user, hoaDon, amount, description);
            
            response.put("success", true);
            response.put("message", "Payment created successfully");
            // response.put("transaction", transaction);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error creating SePay payment: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Error creating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API để kiểm tra trạng thái giao dịch
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Map<String, Object>> getTransactionStatus(@PathVariable String transactionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var transaction = sePayService.checkTransactionStatus(transactionId);
            
            if (transaction != null) {
                response.put("success", true);
                response.put("transaction", Map.of(
                    "id", transaction.getId(),
                    "transactionId", transaction.getTransactionId(),
                    "orderId", transaction.getOrderId(),
                    "amount", transaction.getAmount(),
                    "status", transaction.getStatus(),
                    "qrCodeUrl", transaction.getQrCodeUrl(),
                    "createdAt", transaction.getCreatedAt(),
                    "updatedAt", transaction.getUpdatedAt()
                ));
            } else {
                response.put("success", false);
                response.put("message", "Transaction not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error getting transaction status: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Error getting transaction status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API để hủy giao dịch
     */
    @PostMapping("/cancel/{transactionId}")
    public ResponseEntity<Map<String, Object>> cancelTransaction(@PathVariable String transactionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean cancelled = sePayService.cancelTransaction(transactionId);
            
            if (cancelled) {
                response.put("success", true);
                response.put("message", "Transaction cancelled successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to cancel transaction or transaction not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error cancelling transaction: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Error cancelling transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
