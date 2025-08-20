package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController extends BaseController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showOrdersPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<HoaDon> orders = hoaDonService.getUserOrders(user);
        model.addAttribute("orders", orders);
        return "user/orders";
    }

    @GetMapping("/{orderId}")
    public String showOrderDetail(@PathVariable Integer orderId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        HoaDon order = hoaDonService.getUserOrderById(user, orderId);
        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "user/order-detail";
    }

    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public Map<String, Object> cancelOrder(@PathVariable Integer orderId) {
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

            boolean success = hoaDonService.cancelOrder(user, orderId);
            if (success) {
                return Map.of("success", true, "message", "Hủy đơn hàng thành công");
            } else {
                return Map.of("success", false, "message", "Không thể hủy đơn hàng này");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage());
        }
    }
}
