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
    public String showCheckoutPage(Model model) {
        try {
            CheckoutResponseDTO response = checkoutService.getCheckoutPageDataWithValidation();
            model.addAttribute("cart", response.getCart());
            model.addAttribute("user", response.getUser());
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
}
