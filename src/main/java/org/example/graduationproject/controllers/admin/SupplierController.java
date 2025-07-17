package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.services.NhaCungCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/supplier")
public class SupplierController {
    @Autowired
    private NhaCungCapService nhaCungCapService;

    @GetMapping
    public String supplierPage(Model model,
                               @RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(value = "showForm", defaultValue = "false") boolean showForm,
                               @RequestParam(value = "editId", required = false) Integer editId) {
        Page<NhaCungCap> supplierPage;
        if (!search.isEmpty()) {
            supplierPage = nhaCungCapService.searchNhaCungCapByTenPaging(search, page, size);
        } else {
            supplierPage = nhaCungCapService.getAllNhaCungCapPaging(page, size);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "supplier");
        model.addAttribute("suppliers", supplierPage.getContent());
        model.addAttribute("supplierPage", supplierPage);
        model.addAttribute("totalPages", supplierPage.getTotalPages());
        model.addAttribute("totalElements", supplierPage.getTotalElements());
        int lastPage = supplierPage.getTotalPages() > 0 ? supplierPage.getTotalPages() - 1 : 0;
        int prevPage = (page > 0) ? page - 1 : 0;
        int nextPage = (page + 1 < supplierPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("formAction", "/admin/supplier/add");
        model.addAttribute("showForm", showForm);
        model.addAttribute("editMode", false);
        if (editId != null) {
            NhaCungCap editSupplier = nhaCungCapService.findById(editId).orElse(null);
            if (editSupplier != null) {
                model.addAttribute("supplier", editSupplier);
                model.addAttribute("formAction", "/admin/supplier/update");
                model.addAttribute("showForm", true);
                model.addAttribute("editMode", true);
            } else {
                model.addAttribute("supplier", new NhaCungCap());
            }
        } else {
            model.addAttribute("supplier", new NhaCungCap());
        }
        return "admin/supplier";
    }

    @PostMapping("/add")
    public String addSupplier(@ModelAttribute("supplier") NhaCungCap nhaCungCap, RedirectAttributes redirectAttributes) {
        nhaCungCapService.save(nhaCungCap);
        redirectAttributes.addFlashAttribute("success", "Supplier added successfully!");
        return "redirect:/admin/supplier";
    }

    @PostMapping("/update")
    public String updateSupplier(@ModelAttribute("supplier") NhaCungCap nhaCungCap, RedirectAttributes redirectAttributes) {
        nhaCungCapService.save(nhaCungCap);
        redirectAttributes.addFlashAttribute("success", "Supplier update successful!");
        return "redirect:/admin/supplier";
    }

    @GetMapping("/edit/{id}")
    public String editSupplier(@PathVariable("id") Integer id) {
        return "redirect:/admin/supplier?editId=" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        nhaCungCapService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Supplier deleted successfully!");
        return "redirect:/admin/supplier";
    }
}
