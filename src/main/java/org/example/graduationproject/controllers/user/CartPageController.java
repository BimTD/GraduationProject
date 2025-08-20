package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.services.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartPageController extends BaseController {

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/cart")
    public String cartPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                GioHang cart = gioHangService.getActiveCart(user);
                model.addAttribute("cart", cart);
            }
        } else {
            return "redirect:/login";
        }
        return "user/cart";
    }
}
