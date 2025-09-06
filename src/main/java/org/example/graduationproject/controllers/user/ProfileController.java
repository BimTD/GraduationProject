package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.models.CustomUserDetails;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController extends BaseController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public String showProfilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Thông tin cá nhân");
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam("hoTen") String hoTen,
                               @RequestParam("email") String email,
                               @RequestParam("soDienThoai") String soDienThoai,
                               @RequestParam("diaChi") String diaChi,
                               RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userDetails.getUser();
            ProfileService.UpdateResult result = profileService.updateProfile(
                user.getId(), hoTen, email, soDienThoai, diaChi
            );

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật thông tin!");
        }

        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userDetails.getUser();
            ProfileService.PasswordChangeResult result = profileService.changePassword(
                user.getId(), currentPassword, newPassword, confirmPassword
            );

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi đổi mật khẩu!");
        }

        return "redirect:/profile";
    }
}
