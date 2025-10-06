package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.HomeResponseDTO;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.services.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/home")
public class UserController extends BaseController {

    @Autowired
    private HomeService homeService;

    @GetMapping
    public String homePage(Model model) {
        try {
            HomeResponseDTO response = homeService.getHomePageDataWithValidation();
            if (response.isSuccess()) {
                model.addAttribute("sanPhamNamTheoLoai", response.getSanPhamNamTheoLoai());
                model.addAttribute("sanPhamNuTheoLoai", response.getSanPhamNuTheoLoai());
                model.addAttribute("products", response.getProducts());
                model.addAttribute("newestProducts", response.getNewestProducts());
            } else {
                // Fallback nếu có lỗi
                model.addAttribute("sanPhamNamTheoLoai", Map.of());
                model.addAttribute("sanPhamNuTheoLoai", Map.of());
                model.addAttribute("products", List.of());
                model.addAttribute("newestProducts", List.of());
            }
        } catch (Exception e) {
            // Fallback nếu có exception
            model.addAttribute("sanPhamNamTheoLoai", Map.of());
            model.addAttribute("sanPhamNuTheoLoai", Map.of());
            model.addAttribute("products", List.of());
            model.addAttribute("newestProducts", List.of());
        }
        
        return "user/home";
    }
}
