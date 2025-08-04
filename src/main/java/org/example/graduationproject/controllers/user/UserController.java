package org.example.graduationproject.controllers.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.models.SanPham;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping("/home")
    public String homePage(Model model) {
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

        List<SanPham> sanPhamNam = sanPhamService.getByGioiTinh(1);
        List<SanPham> sanPhamNu = sanPhamService.getByGioiTinh(2);
        model.addAttribute("sanPhamNam", sanPhamNam);
        model.addAttribute("sanPhamNu", sanPhamNu);

        return "user/home";
    }
}
