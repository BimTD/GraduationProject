package org.example.graduationproject.controllers.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.services.LoaiService;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.Loai;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class UserController {

    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private LoaiService loaiService;

    @GetMapping("/home")
    public String homePage(Model model) {
        // Xử lý authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                model.addAttribute("username", ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername());
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                model.addAttribute("username", ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getAttribute("email"));
            } else {
                model.addAttribute("username", authentication.getName());
            }
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }

        // Lấy tất cả loại sản phẩm
        List<Loai> allLoai = loaiService.getAllLoai();

        // Lấy sản phẩm theo giới tính và loại cho từng tab
        Map<String, List<SanPham>> sanPhamNamTheoLoai = new HashMap<>();
        Map<String, List<SanPham>> sanPhamNuTheoLoai = new HashMap<>();
        
        for (Loai loai : allLoai) {
            String tenLoai = loai.getTen().toLowerCase();
            
            // Lấy sản phẩm nam theo loại
            List<SanPham> spNamTheoLoai = sanPhamService.filterByCategoryAndGenderPaging(loai.getId(), 1, 0, 100).getContent();
            sanPhamNamTheoLoai.put(tenLoai, spNamTheoLoai);
            
            // Lấy sản phẩm nữ theo loại
            List<SanPham> spNuTheoLoai = sanPhamService.filterByCategoryAndGenderPaging(loai.getId(), 2, 0, 100).getContent();
            sanPhamNuTheoLoai.put(tenLoai, spNuTheoLoai);
        }
        
        model.addAttribute("sanPhamNamTheoLoai", sanPhamNamTheoLoai);
        model.addAttribute("sanPhamNuTheoLoai", sanPhamNuTheoLoai);

        return "user/home";
    }
}
