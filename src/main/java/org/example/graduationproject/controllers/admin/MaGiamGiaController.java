package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.services.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/ma-giam-gia")
public class MaGiamGiaController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @GetMapping
    public String redirectToDiscount() {
        return "redirect:/admin/ma-giam-gia/discount";
    }

    @GetMapping("/discount")
    public String discountPage(Model model, 
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String status) {
        List<MaGiamGia> maGiamGiaList = maGiamGiaService.getAllMaGiamGia();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            maGiamGiaList = maGiamGiaList.stream()
                .filter(mgg -> mgg.getMaGiamGia().toLowerCase().contains(search.toLowerCase()) ||
                              mgg.getTenMaGiamGia().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        // Apply status filter
        if (status != null && !status.trim().isEmpty()) {
            maGiamGiaList = maGiamGiaList.stream()
                .filter(mgg -> mgg.getTrangThai().toString().equals(status))
                .toList();
        }
        
        model.addAttribute("maGiamGiaList", maGiamGiaList);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", "discount");
        model.addAttribute("maGiamGia", new MaGiamGia());
        return "admin/discount";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("maGiamGia", new MaGiamGia());
        model.addAttribute("maGiamGiaList", maGiamGiaService.getAllMaGiamGia());
        model.addAttribute("currentPage", "discount");
        return "admin/discount";
    }

    @PostMapping("/create")
    public String createMaGiamGia(@ModelAttribute MaGiamGia maGiamGia, RedirectAttributes redirectAttributes) {
        try {
            maGiamGiaService.createMaGiamGia(maGiamGia);
            redirectAttributes.addFlashAttribute("success", "Tạo mã giảm giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/ma-giam-gia/discount";
    }

    @GetMapping("/edit/{id:[0-9]+}")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            MaGiamGia maGiamGia = maGiamGiaService.getMaGiamGiaById(id);
            model.addAttribute("maGiamGia", maGiamGia);
            model.addAttribute("maGiamGiaList", maGiamGiaService.getAllMaGiamGia());
            model.addAttribute("currentPage", "discount");
            return "admin/discount";
        } catch (Exception e) {
            return "redirect:/admin/ma-giam-gia/discount";
        }
    }

    @PostMapping("/edit/{id:[0-9]+}")
    public String updateMaGiamGia(@PathVariable Integer id, @ModelAttribute MaGiamGia maGiamGia, 
                                 RedirectAttributes redirectAttributes) {
        try {
            maGiamGia.setId(id);
            maGiamGiaService.updateMaGiamGia(maGiamGia);
            redirectAttributes.addFlashAttribute("success", "Cập nhật mã giảm giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/ma-giam-gia/discount";
    }

    @GetMapping("/delete/{id:[0-9]+}")
    public String deleteMaGiamGia(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            maGiamGiaService.deleteMaGiamGia(id);
            redirectAttributes.addFlashAttribute("success", "Xóa mã giảm giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/ma-giam-gia/discount";
    }

    @GetMapping("/update-status/{id:[0-9]+}")
    public String updateStatus(@PathVariable Integer id, @RequestParam String status, 
                              RedirectAttributes redirectAttributes) {
        try {
            maGiamGiaService.updateMaGiamGiaStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/ma-giam-gia/discount";
    }

    @PostMapping("/update-expired")
    public String updateExpired(RedirectAttributes redirectAttributes) {
        try {
            maGiamGiaService.updateExpiredMaGiamGia();
            redirectAttributes.addFlashAttribute("success", "Cập nhật mã giảm giá hết hạn thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/ma-giam-gia/discount";
    }

    @GetMapping("/test/{code}")
    @ResponseBody
    public String testMaGiamGia(@PathVariable String code) {
        try {
            boolean isValid = maGiamGiaService.isValidMaGiamGia(code);
            return "Mã giảm giá '" + code + "' " + (isValid ? "hợp lệ" : "không hợp lệ");
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}
