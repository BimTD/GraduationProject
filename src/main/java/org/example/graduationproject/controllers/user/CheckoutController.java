package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.services.GioHangService;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController extends BaseController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showCheckoutPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy giỏ hàng active
        var cart = gioHangService.getActiveCart(user);
        if (cart == null || cart.getChiTietGioHangs() == null || cart.getChiTietGioHangs().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        return "user/checkout";
    }

    @PostMapping("/process")
    @ResponseBody
    public Map<String, Object> processCheckout(@RequestBody CheckoutDTO checkoutDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Map.of("success", false, "message", "Vui lòng đăng nhập");
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return Map.of("success", false, "message", "Không tìm thấy thông tin người dùng");
            }

            // Tạo đơn hàng
            HoaDon hoaDon = hoaDonService.createOrderFromCart(user, checkoutDTO);

            return Map.of(
                "success", true, 
                "message", "Đặt hàng thành công! Mã đơn hàng: " + hoaDon.getId(),
                "orderId", hoaDon.getId()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage());
        }
    }
}
