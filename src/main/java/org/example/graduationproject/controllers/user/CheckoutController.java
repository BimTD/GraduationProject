package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.SePayTransaction;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.AuthenticationService;
import org.example.graduationproject.services.CheckoutService;
import org.example.graduationproject.services.SePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController extends BaseController {

    @Autowired
    private CheckoutService checkoutService;
    
    @Autowired
    private SePayService sePayService;
    
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public String showCheckoutPage(Model model, 
                                  @RequestParam(required = false) String discountCode,
                                  @RequestParam(required = false) String discountAmount,
                                  @RequestParam(required = false) String discountCodes,
                                  @RequestParam(required = false) String totalDiscountAmount) {
        try {
            CheckoutResponseDTO response = checkoutService.getCheckoutPageDataWithValidation();
            model.addAttribute("cart", response.getCart());
            model.addAttribute("user", response.getUser());
            
            // Tính tổng tiền gốc
            java.math.BigDecimal originalTotal = response.getCart().getChiTietGioHangs().stream()
                .map(org.example.graduationproject.models.ChiTietGioHang::getThanhTien)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            // Xử lý mã giảm giá (hỗ trợ cả 1 mã và nhiều mã)
            if (discountCodes != null && !discountCodes.trim().isEmpty()) {
                // Nhiều mã giảm giá (từ cart mới)
                String[] codes = discountCodes.split(",");
                model.addAttribute("appliedDiscountCodes", codes);
                model.addAttribute("discountCodes", discountCodes);
                
                if (totalDiscountAmount != null && !totalDiscountAmount.trim().isEmpty()) {
                    try {
                        java.math.BigDecimal totalDiscountBD = new java.math.BigDecimal(totalDiscountAmount);
                        java.math.BigDecimal finalTotal = originalTotal.subtract(totalDiscountBD);
                        model.addAttribute("totalDiscountAmount", totalDiscountBD);
                        model.addAttribute("finalTotal", finalTotal);
                    } catch (NumberFormatException e) {
                        model.addAttribute("finalTotal", originalTotal);
                    }
                }
            } else if (discountCode != null && !discountCode.trim().isEmpty()) {
                // 1 mã giảm giá (từ cart cũ)
                model.addAttribute("discountCode", discountCode);
                model.addAttribute("discountAmount", discountAmount);
                
                if (discountAmount != null && !discountAmount.trim().isEmpty()) {
                    try {
                        java.math.BigDecimal discountAmountBD = new java.math.BigDecimal(discountAmount);
                        java.math.BigDecimal finalTotal = originalTotal.subtract(discountAmountBD);
                        model.addAttribute("finalTotal", finalTotal);
                    } catch (NumberFormatException e) {
                        model.addAttribute("finalTotal", originalTotal);
                    }
                }
            } else {
                // Không có mã giảm giá
                model.addAttribute("finalTotal", originalTotal);
            }
            
        } catch (Exception e) {
            return "redirect:/cart";
        }
        return "user/checkout";
    }

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<CheckoutResponseDTO> processCheckout(@RequestBody CheckoutDTO checkoutDTO) {
        CheckoutResponseDTO response = checkoutService.processCheckoutWithValidation(checkoutDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-discount")
    @ResponseBody
    public ResponseEntity<CheckoutResponseDTO> validateDiscountCode(@RequestParam String maGiamGia) {
        CheckoutResponseDTO response = checkoutService.validateMaGiamGia(maGiamGia);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo giao dịch thanh toán SePay
     */
    @PostMapping("/create-sepay-payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSePayPayment(@RequestBody Map<String, Object> paymentData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Lấy thông tin từ request
            Integer orderId = (Integer) paymentData.get("orderId");
            String description = (String) paymentData.get("description");
            BigDecimal amount = new BigDecimal(paymentData.get("amount").toString());
            
            if (orderId == null || amount == null) {
                response.put("success", false);
                response.put("message", "Order ID and amount are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Lấy thông tin user và hóa đơn
            if (!authenticationService.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }
            
            User user = authenticationService.getCurrentUser();
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(401).body(response);
            }
            
            // Tìm hóa đơn
            HoaDon hoaDon = checkoutService.getHoaDonById(orderId);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Order not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Tạo giao dịch SePay
            SePayTransaction transaction = sePayService.createPayment(user, hoaDon, amount, description);
            
            response.put("success", true);
            response.put("message", "SePay payment created successfully");
            response.put("transaction", Map.of(
                "id", transaction.getId(),
                "transactionId", transaction.getTransactionId(),
                "qrCodeUrl", transaction.getQrCodeUrl(),
                "amount", transaction.getAmount(),
                "status", transaction.getStatus()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error creating SePay payment: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Error creating payment: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Kiểm tra trạng thái giao dịch SePay
     */
    @GetMapping("/sepay-status/{transactionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSePayStatus(@PathVariable String transactionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            SePayTransaction transaction = sePayService.checkTransactionStatus(transactionId);
            
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
            System.err.println("Error getting SePay status: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Error getting transaction status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
