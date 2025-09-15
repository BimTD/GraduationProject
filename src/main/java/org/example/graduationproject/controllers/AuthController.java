package org.example.graduationproject.controllers;

import org.example.graduationproject.services.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", "Tên người dùng hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công!");
        }

        if (registered != null) {
            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        }

        return "login";
    }

    @GetMapping("/oauth2/login")
    public String oauth2Login() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/oauth2/success")
    public String oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {
        if (oauth2User != null) {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            model.addAttribute("email", email);
            model.addAttribute("name", name);
        }
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                              @RequestParam("email") String email,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model) {

        // Kiểm tra password và confirm password
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "register";
        }

        // Đăng ký user
        UserRegistrationService.RegistrationResult result = userRegistrationService.registerUser(username, email, password);

        if (result.isSuccess()) {
            model.addAttribute("message", result.getMessage());
            return "redirect:/login?registered=true";
        } else {
            model.addAttribute("error", result.getMessage());
            return "register";
        }
    }
}
