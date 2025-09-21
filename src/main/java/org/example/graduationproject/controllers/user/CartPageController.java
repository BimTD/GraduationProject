package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.CartPageResponseDTO;
import org.example.graduationproject.services.CartPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartPageController extends BaseController {

    @Autowired
    private CartPageService cartPageService;

    @GetMapping("/cart")
    public String cartPage(Model model, @RequestParam(required = false) String expired) {
        try {
            CartPageResponseDTO response = cartPageService.getCartPageDataWithValidation();
            model.addAttribute("cart", response.getCart());
            model.addAttribute("user", response.getUser());
            
            // Kiểm tra thời gian hết hạn thanh toán
            boolean isPaymentExpired = cartPageService.isCartPaymentExpired();
            model.addAttribute("isPaymentExpired", isPaymentExpired);
            
            // Thông báo từ checkout
            if ("true".equals(expired)) {
                model.addAttribute("expiredMessage", "Thời gian thanh toán đã hết hạn. Vui lòng thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm.");
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "user/cart";
    }
}
