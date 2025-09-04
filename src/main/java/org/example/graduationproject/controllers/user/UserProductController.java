package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.ProductResponseDTO;
import org.example.graduationproject.services.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product-details")
public class UserProductController extends BaseController {

    @Autowired
    private UserProductService userProductService;

    @GetMapping("/{id}")
    public String productDetails(@PathVariable("id") Integer productId, Model model) {
        try {
            ProductResponseDTO response = userProductService.getProductDetailsWithValidation(productId);
            if (response.isSuccess()) {
                model.addAttribute("product", response.getData());
            } else {
                model.addAttribute("error", response.getMessage());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin sản phẩm: " + e.getMessage());
        }
        
        return "user/product-details";
    }
}


