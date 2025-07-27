package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.services.NhanHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brand")
public class BrandController {
    @Autowired
    private NhanHieuService nhanHieuService;

    @GetMapping
    public String brandPage(Model model,
                            @RequestParam(defaultValue = "") String search,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Page<NhanHieu> brandPage;
        if (!search.isEmpty()) {
            brandPage = nhanHieuService.searchNhanHieuByTenPaging(search, page, size);
        } else {
            brandPage = nhanHieuService.getAllNhanHieuPaging(page, size);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "brand");
        model.addAttribute("brands", brandPage.getContent());
        model.addAttribute("brandPage", brandPage);
        model.addAttribute("totalPages", brandPage.getTotalPages());
        model.addAttribute("totalElements", brandPage.getTotalElements());
        int lastPage = brandPage.getTotalPages() > 0 ? brandPage.getTotalPages() - 1 : 0;
        int prevPage = (page > 0) ? page - 1 : 0;
        int nextPage = (page + 1 < brandPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("brand", new NhanHieu());
        model.addAttribute("formAction", "/admin/brand/add");
        model.addAttribute("editMode", false);
        return "admin/brand";
    }

    // Thêm category
    @PostMapping("/add")
    public String addBrand(@ModelAttribute("brand") NhanHieu nhanHieu, RedirectAttributes redirectAttributes) {
        nhanHieuService.save(nhanHieu);
        redirectAttributes.addFlashAttribute("success", "Brand added successfully!");
        return "redirect:/admin/brand";
    }

    @GetMapping("/edit/{id}")
    public String editBrandPage(@PathVariable("id") Integer id, Model model,
                                @RequestParam(defaultValue = "") String search,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        NhanHieu nhanHieu = nhanHieuService.findById(id).orElse(null);
        Page<NhanHieu> brandPage;
        if (!search.isEmpty()) {
            brandPage = nhanHieuService.searchNhanHieuByTenPaging(search, page, size);
        } else {
            brandPage = nhanHieuService.getAllNhanHieuPaging(page, size);
        }
        model.addAttribute("brands", brandPage.getContent());
        model.addAttribute("brandPage", brandPage);
        model.addAttribute("totalPages", brandPage.getTotalPages());
        model.addAttribute("totalElements", brandPage.getTotalElements());
        int lastPage = brandPage.getTotalPages() > 0 ? brandPage.getTotalPages() - 1 : 0;
        int prevPage = (page > 0) ? page - 1 : 0;
        int nextPage = (page + 1 < brandPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("brand", nhanHieu);
        model.addAttribute("formAction", "/admin/brand/update");
        model.addAttribute("editMode", true);
        return "admin/brand";
    }

    @PostMapping("/update")
    public String updateBrand(@ModelAttribute("brand") NhanHieu nhanHieu, RedirectAttributes redirectAttributes) {
        nhanHieuService.save(nhanHieu);
        redirectAttributes.addFlashAttribute("success", "Brand update successful!");
        return "redirect:/admin/brand";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        nhanHieuService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Brand removal successful!");
        return "redirect:/admin/brand";
    }
}
