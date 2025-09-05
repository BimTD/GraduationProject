package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.ShopResponseDTO;
import org.example.graduationproject.services.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop")
public class ShopController extends BaseController {

    @Autowired
    private ShopService shopService;

    @GetMapping
    public String shopPage(Model model,
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "categoryId", required = false) Integer categoryId,
                          @RequestParam(value = "gender", required = false) String gender,
                          @RequestParam(value = "colorId", required = false) Integer colorId,
                          @RequestParam(value = "tag", required = false) String tag,
                          @RequestParam(value = "brandId", required = false) Integer brandId,
                          @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                          @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                          @RequestParam(value = "sort", required = false) String sort,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "12") int size) {
        try {
            ShopResponseDTO response = shopService.getShopPageDataWithValidation(
                search, categoryId, gender, colorId, tag, brandId, minPrice, maxPrice, sort, page, size);
            
            if (response.isSuccess()) {
                model.addAttribute("products", response.getProducts());
                model.addAttribute("categories", response.getCategories());
                model.addAttribute("brands", response.getBrands());
                model.addAttribute("colors", response.getColors());
                model.addAttribute("popularTags", response.getPopularTags());
                model.addAttribute("totalProducts", response.getTotalProducts());
            } else {
                // Fallback nếu có lỗi
                model.addAttribute("products", List.of());
                model.addAttribute("categories", List.of());
                model.addAttribute("brands", List.of());
                model.addAttribute("colors", List.of());
                model.addAttribute("popularTags", List.of());
                model.addAttribute("totalProducts", 0);
            }
        } catch (Exception e) {
            // Fallback nếu có exception
            model.addAttribute("products", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("brands", List.of());
            model.addAttribute("colors", List.of());
            model.addAttribute("popularTags", List.of());
            model.addAttribute("totalProducts", 0);
        }
        
        return "user/shop";
    }

}
