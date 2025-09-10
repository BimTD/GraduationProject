package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.services.MaGiamGiaService;
import org.example.graduationproject.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/discount-codes")
public class UserDiscountController extends BaseController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;
    
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public String showDiscountCodesPage(Model model) {
        try {
            // Lấy user hiện tại
            org.example.graduationproject.models.User currentUser = null;
            try {
                currentUser = authenticationService.getCurrentUser();
            } catch (Exception e) {
                System.out.println("User chưa đăng nhập: " + e.getMessage());
            }
            
            List<MaGiamGia> availableDiscountCodes;
            
            if (currentUser != null) {
                // Lấy mã giảm giá có thể sử dụng cho user (chưa sử dụng)
                availableDiscountCodes = maGiamGiaService.getAvailableMaGiamGiaForUser(currentUser);
                System.out.println("Số lượng mã giảm giá có thể sử dụng cho user " + currentUser.getId() + ": " + availableDiscountCodes.size());
            } else {
                // Nếu chưa đăng nhập, hiển thị tất cả mã active
                availableDiscountCodes = maGiamGiaService.getActiveMaGiamGia();
                System.out.println("Số lượng mã giảm giá active (chưa đăng nhập): " + availableDiscountCodes.size());
            }
            
            model.addAttribute("discountCodes", availableDiscountCodes);
            model.addAttribute("currentPage", "discount-codes");
            model.addAttribute("isLoggedIn", currentUser != null);
            
            return "user/discount-codes";
        } catch (Exception e) {
            // Log error
            System.err.println("Lỗi khi tải mã giảm giá: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải mã giảm giá: " + e.getMessage());
            return "user/discount-codes";
        }
    }
}
