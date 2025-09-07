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
                                  @RequestParam(required = false) String discountAmount) {
        try {
            CheckoutResponseDTO response = checkoutService.getCheckoutPageDataWithValidation();
            model.addAttribute("cart", response.getCart());
            model.addAttribute("user", response.getUser());
            
            // Thêm thông tin mã giảm giá nếu có
            if (discountCode != null && !discountCode.trim().isEmpty()) {
                model.addAttribute("discountCode", discountCode);
                model.addAttribute("discountAmount", discountAmount);
                
                // Cập nhật tổng tiền trong model nếu có mã giảm giá
                if (discountAmount != null && !discountAmount.trim().isEmpty()) {
                    try {
                        java.math.BigDecimal discountAmountBD = new java.math.BigDecimal(discountAmount);
                        java.math.BigDecimal originalTotal = response.getCart().getChiTietGioHangs().stream()
                            .map(org.example.graduationproject.models.ChiTietGioHang::getThanhTien)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                        java.math.BigDecimal finalTotal = originalTotal.subtract(discountAmountBD);
                        model.addAttribute("finalTotal", finalTotal);
                    } catch (NumberFormatException e) {
                        // Nếu không parse được số, sử dụng tổng tiền gốc
                        model.addAttribute("finalTotal", response.getCart().getChiTietGioHangs().stream()
                            .map(org.example.graduationproject.models.ChiTietGioHang::getThanhTien)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
                    }
                }
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
