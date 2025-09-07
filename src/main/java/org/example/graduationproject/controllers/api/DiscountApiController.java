package org.example.graduationproject.controllers.api;

import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.services.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ma-giam-gia")
public class DiscountApiController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateMaGiamGia(
            @RequestParam String maGiamGia,
            @RequestParam BigDecimal orderTotal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = maGiamGiaService.isValidMaGiamGia(maGiamGia);
            
            if (isValid) {
                BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(
                    maGiamGia, orderTotal, null);
                
                response.put("success", true);
                response.put("valid", true);
                response.put("discountAmount", discountAmount);
                response.put("message", "Mã giảm giá hợp lệ");
            } else {
                response.put("success", true);
                response.put("valid", false);
                response.put("discountAmount", BigDecimal.ZERO);
                response.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("valid", false);
            response.put("discountAmount", BigDecimal.ZERO);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveMaGiamGia() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MaGiamGia> activeCodes = maGiamGiaService.getActiveMaGiamGia();
            response.put("success", true);
            response.put("data", activeCodes);
            response.put("message", "Lấy danh sách mã giảm giá thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applicable")
    public ResponseEntity<Map<String, Object>> getApplicableMaGiamGia(
            @RequestParam(required = false) List<Integer> productIds) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MaGiamGia> applicableCodes;
            
            if (productIds == null || productIds.isEmpty()) {
                applicableCodes = maGiamGiaService.getActiveMaGiamGia();
            } else {
                // TODO: Implement getApplicableMaGiamGiaForProductIds if needed
                applicableCodes = maGiamGiaService.getActiveMaGiamGia();
            }
            
            response.put("success", true);
            response.put("data", applicableCodes);
            response.put("message", "Lấy danh sách mã giảm giá áp dụng được thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateDiscount(
            @RequestParam String maGiamGia,
            @RequestParam BigDecimal orderTotal,
            @RequestParam(required = false) List<Integer> productIds) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(
                maGiamGia, orderTotal, null);
            
            BigDecimal finalTotal = orderTotal.subtract(discountAmount);
            
            response.put("success", true);
            response.put("originalTotal", orderTotal);
            response.put("discountAmount", discountAmount);
            response.put("finalTotal", finalTotal);
            response.put("message", "Tính toán giảm giá thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
