package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/user")
    public String userPage(Model model,
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "admin";
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "user");

        // Load all users
        List<User> allUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // Apply search filter
        if (search != null && !search.isBlank()) {
            final String q = search.toLowerCase(Locale.ROOT).trim();
            allUsers = allUsers.stream().filter(user -> {
                String idStr = user.getId() != null ? String.valueOf(user.getId()) : "";
                String usernameStr = user.getUsername() != null ? user.getUsername().toLowerCase(Locale.ROOT) : "";
                String emailStr = user.getEmail() != null ? user.getEmail().toLowerCase(Locale.ROOT) : "";
                String hoTenStr = user.getHoTen() != null ? user.getHoTen().toLowerCase(Locale.ROOT) : "";
                String phoneStr = user.getSoDienThoai() != null ? user.getSoDienThoai().toLowerCase(Locale.ROOT) : "";
                
                return idStr.contains(q) || usernameStr.contains(q) || emailStr.contains(q) || 
                       hoTenStr.contains(q) || phoneStr.contains(q);
            }).collect(Collectors.toList());
        }

        // Apply status filter
        if (status != null && !status.isBlank()) {
            if ("enabled".equals(status)) {
                allUsers = allUsers.stream()
                    .filter(user -> "1".equals(user.getEnabled()))
                    .collect(Collectors.toList());
            } else if ("disabled".equals(status)) {
                allUsers = allUsers.stream()
                    .filter(user -> "0".equals(user.getEnabled()))
                    .collect(Collectors.toList());
            }
        }

        // Calculate pagination
        long totalElements = allUsers.size();
        int totalPages = (int) Math.ceil(totalElements / (double) size);
        int lastPage = totalPages > 0 ? totalPages - 1 : 0;
        int prevPage = page > 0 ? page - 1 : 0;
        int nextPage = (page + 1 < totalPages) ? page + 1 : lastPage;

        // Get page content
        int fromIndex = Math.min(page * size, allUsers.size());
        int toIndex = Math.min(fromIndex + size, allUsers.size());
        List<User> pageContent = fromIndex < toIndex ? allUsers.subList(fromIndex, toIndex) : List.of();

        model.addAttribute("users", pageContent);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "admin/user";
    }

    @PostMapping("/user/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
                return "redirect:/admin/user";
            }

            // Toggle enabled status
            String currentStatus = user.getEnabled();
            String newStatus = "1".equals(currentStatus) ? "0" : "1";
            user.setEnabled(newStatus);
            userRepository.save(user);

            String statusText = "1".equals(newStatus) ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("success", "Đã " + statusText + " người dùng " + user.getUsername() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái người dùng: " + e.getMessage());
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/user/{id}")
    public String userDetail(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/user";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "admin";
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "user");
        model.addAttribute("user", user);

        return "admin/user-detail";
    }

    @PostMapping("/user/{id}/reset-password")
    public String resetUserPassword(@PathVariable("id") Long id, 
                                   @RequestParam("newPassword") String newPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
                return "redirect:/admin/user";
            }

            // Kiểm tra mật khẩu mới
            if (newPassword == null || newPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được để trống.");
                return "redirect:/admin/user/" + id;
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự.");
                return "redirect:/admin/user/" + id;
            }

            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Đã đặt lại mật khẩu cho người dùng " + user.getUsername() + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đặt lại mật khẩu: " + e.getMessage());
        }
        return "redirect:/admin/user/" + id;
    }
}
