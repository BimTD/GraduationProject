package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.CartPageResponseDTO;
import org.example.graduationproject.services.CartPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartPageController extends BaseController {

    @Autowired
    private CartPageService cartPageService;

    @GetMapping("/cart")
    public String cartPage(Model model) {
        try {
            CartPageResponseDTO response = cartPageService.getCartPageDataWithValidation();
            model.addAttribute("cart", response.getCart());
            model.addAttribute("user", response.getUser());
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "user/cart";
    }
}
