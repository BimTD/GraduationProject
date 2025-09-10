package org.example.graduationproject.controllers.api;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.MaGiamGiaDTO;
import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.services.MaGiamGiaService;
import org.example.graduationproject.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ma-giam-gia")
public class DiscountApiController extends BaseController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;
    
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateMaGiamGia(
            @RequestParam String maGiamGia,
            @RequestParam BigDecimal orderTotal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Lấy user hiện tại
            org.example.graduationproject.models.User currentUser = null;
            try {
                currentUser = authenticationService.getCurrentUser();
            } catch (Exception e) {
                // User chưa đăng nhập
            }
            
            // Kiểm tra mã giảm giá có hợp lệ không
            boolean isValid = maGiamGiaService.isValidMaGiamGia(maGiamGia);
            
            if (!isValid) {
                response.put("success", true);
                response.put("valid", false);
                response.put("discountAmount", BigDecimal.ZERO);
                response.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
                return ResponseEntity.ok(response);
            }
            
            // Kiểm tra user đã sử dụng mã này chưa
            if (currentUser != null && !maGiamGiaService.canUserUseMaGiamGia(maGiamGia, currentUser)) {
                response.put("success", true);
                response.put("valid", false);
                response.put("discountAmount", BigDecimal.ZERO);
                response.put("message", "Bạn đã sử dụng mã giảm giá này rồi");
                return ResponseEntity.ok(response);
            }
            
            // Tính giá trị giảm giá
            BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(
                maGiamGia, orderTotal, null);
            
            response.put("success", true);
            response.put("valid", true);
            response.put("discountAmount", discountAmount);
            response.put("message", "Mã giảm giá hợp lệ");
            
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
            List<MaGiamGiaDTO> dtoList = activeCodes.stream()
                .map(MaGiamGiaDTO::new)
                .collect(Collectors.toList());
            response.put("success", true);
            response.put("data", dtoList);
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
            
            List<MaGiamGiaDTO> dtoList = applicableCodes.stream()
                .map(MaGiamGiaDTO::new)
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("data", dtoList);
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

    // API mới: Validate nhiều mã giảm giá
    @PostMapping("/validate-multiple")
    public ResponseEntity<Map<String, Object>> validateMultipleMaGiamGia(
            @RequestParam List<String> maGiamGiaList,
            @RequestParam BigDecimal orderTotal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> validCodes = new java.util.ArrayList<>();
            List<Map<String, Object>> invalidCodes = new java.util.ArrayList<>();
            BigDecimal totalDiscount = BigDecimal.ZERO;
            
            for (String maGiamGia : maGiamGiaList) {
                Map<String, Object> codeResult = new HashMap<>();
                codeResult.put("maGiamGia", maGiamGia);
                
                boolean isValid = maGiamGiaService.isValidMaGiamGia(maGiamGia);
                
                if (isValid) {
                    BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(
                        maGiamGia, orderTotal, null);
                    
                    codeResult.put("valid", true);
                    codeResult.put("discountAmount", discountAmount);
                    codeResult.put("message", "Mã giảm giá hợp lệ");
                    
                    validCodes.add(codeResult);
                    totalDiscount = totalDiscount.add(discountAmount);
                } else {
                    codeResult.put("valid", false);
                    codeResult.put("discountAmount", BigDecimal.ZERO);
                    codeResult.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
                    
                    invalidCodes.add(codeResult);
                }
            }
            
            BigDecimal finalTotal = orderTotal.subtract(totalDiscount);
            
            response.put("success", true);
            response.put("validCodes", validCodes);
            response.put("invalidCodes", invalidCodes);
            response.put("originalTotal", orderTotal);
            response.put("totalDiscount", totalDiscount);
            response.put("finalTotal", finalTotal);
            response.put("message", String.format("Đã validate %d mã giảm giá", maGiamGiaList.size()));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // API mới: Thêm mã giảm giá vào danh sách
    @PostMapping("/add-to-cart")
    public ResponseEntity<Map<String, Object>> addDiscountToCart(
            @RequestParam String maGiamGia,
            @RequestParam BigDecimal orderTotal,
            @RequestParam(required = false) List<String> existingCodes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra mã đã tồn tại chưa
            if (existingCodes != null && existingCodes.contains(maGiamGia)) {
                response.put("success", false);
                response.put("message", "Mã giảm giá đã được áp dụng");
                return ResponseEntity.ok(response);
            }
            
            // Validate mã giảm giá
            boolean isValid = maGiamGiaService.isValidMaGiamGia(maGiamGia);
            
            if (isValid) {
                BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(
                    maGiamGia, orderTotal, null);
                
                response.put("success", true);
                response.put("maGiamGia", maGiamGia);
                response.put("discountAmount", discountAmount);
                response.put("message", "Mã giảm giá đã được thêm");
            } else {
                response.put("success", false);
                response.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
