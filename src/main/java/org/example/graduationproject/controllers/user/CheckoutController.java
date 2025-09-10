package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;
import org.example.graduationproject.services.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkout")
public class CheckoutController extends BaseController {

    @Autowired
    private CheckoutService checkoutService;

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
}
