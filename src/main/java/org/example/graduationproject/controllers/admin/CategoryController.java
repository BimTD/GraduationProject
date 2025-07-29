package org.example.graduationproject.controllers.admin;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.services.LoaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    @Autowired
    private LoaiService loaiService;

    @GetMapping("/category")
    public String categoryPage(Model model,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "category");
        Page<Loai> categoryPage;
        if (search != null && !search.trim().isEmpty()) {
            categoryPage = loaiService.searchLoaiByTenPaging(search, page, size);
            model.addAttribute("search", search);
        } else {
            categoryPage = loaiService.getAllLoaiPaging(page, size);
        }
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalElements", categoryPage.getTotalElements());
        int lastPage = categoryPage.getTotalPages() > 0 ? categoryPage.getTotalPages() - 1 : 0;
        model.addAttribute("lastPage", lastPage);
        int prevPage = page > 0 ? page - 1 : 0;
        int nextPage = (page + 1 < categoryPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("category", new Loai());
        model.addAttribute("editMode", false);
        model.addAttribute("formAction", "/admin/category/add");
        return "admin/category";
    }

    @PostMapping("/category/add")
    public String addCategory(@ModelAttribute("category") Loai loai, RedirectAttributes redirectAttributes) {
        loaiService.saveLoai(loai);
        redirectAttributes.addFlashAttribute("success", "Add category success!");
        return "redirect:/admin/category";
    }

    @GetMapping("/category/edit/{id}")
    public String editCategoryPage(@PathVariable("id") Integer id, Model model,
                                   @RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "5") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Loai loai = loaiService.getLoaiById(id).orElse(null);

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "category");

        Page<Loai> categoryPage;
        if (search != null && !search.trim().isEmpty()) {
            categoryPage = loaiService.searchLoaiByTenPaging(search, page, size);
            model.addAttribute("search", search);
        } else {
            categoryPage = loaiService.getAllLoaiPaging(page, size);
        }
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalElements", categoryPage.getTotalElements());
        int lastPage = categoryPage.getTotalPages() > 0 ? categoryPage.getTotalPages() - 1 : 0;
        model.addAttribute("lastPage", lastPage);
        int prevPage = page > 0 ? page - 1 : 0;
        int nextPage = (page + 1 < categoryPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);

        model.addAttribute("category", loai); // cho form sửa
        model.addAttribute("editMode", true);
        model.addAttribute("formAction", "/admin/category/update");
        return "admin/category";
    }

    @PostMapping("/category/update")
    public String updateCategory(@ModelAttribute("category") Loai loai, RedirectAttributes redirectAttributes) {
        loaiService.saveLoai(loai);
        redirectAttributes.addFlashAttribute("success", "Update category success!");
        return "redirect:/admin/category";
    }

    //Xóa category
    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        loaiService.deleteLoai(id);
        redirectAttributes.addFlashAttribute("success", "Delete category success!");
        return "redirect:/admin/category";
    }
}
