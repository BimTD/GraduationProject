package org.example.graduationproject.controllers.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.graduationproject.models.MauSac;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.services.MauSacService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ColorController {

    @Autowired
    private MauSacService mauSacService;
    @Autowired
    private LoaiRepository loaiRepository;

    @GetMapping("/color")
    public String colorPage(@RequestParam(value = "editId", required = false) Integer editId,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "5") int size,
                          @RequestParam(value = "search", required = false) String search,
                          Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "color");
        List<Loai> loais = loaiRepository.findAll();
        model.addAttribute("loais", loais);
        Pageable pageable = PageRequest.of(page, size);
        Page<MauSac> colorPage;
        if (search != null && !search.isEmpty()) {
            colorPage = mauSacService.findByMaMauContainingIgnoreCase(search, pageable);
        } else {
            colorPage = mauSacService.findAll(pageable);
        }
        model.addAttribute("colors", colorPage.getContent());
        model.addAttribute("totalElements", colorPage.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", colorPage.getTotalPages());
        model.addAttribute("lastPage", colorPage.getTotalPages() > 0 ? colorPage.getTotalPages() - 1 : 0);
        model.addAttribute("prevPage", page > 0 ? page - 1 : 0);
        model.addAttribute("nextPage", page + 1 < colorPage.getTotalPages() ? page + 1 : colorPage.getTotalPages() - 1);
        model.addAttribute("search", search);
        boolean editMode = false;
        MauSac colorForm = new MauSac();
        String formAction = "/admin/color/add";
        if (editId != null) {
            Optional<MauSac> editColor = mauSacService.findById(editId);
            if (editColor.isPresent()) {
                colorForm = editColor.get();
                editMode = true;
                formAction = "/admin/color/edit";
            }
        }
        model.addAttribute("color", colorForm);
        model.addAttribute("editMode", editMode);
        model.addAttribute("formAction", formAction);
        return "admin/color";
    }

    @PostMapping("/color/add")
    public String addColor(@ModelAttribute MauSac color) {
        if (color.getLoai() != null && color.getLoai().getId() != null) {
            mauSacService.save(color);
        }
        return "redirect:/admin/color";
    }

    @PostMapping("/color/edit")
    public String editColor(@ModelAttribute MauSac color) {
        if (color.getId() != null && color.getLoai() != null && color.getLoai().getId() != null) {
            mauSacService.save(color);
        }
        return "redirect:/admin/color";
    }

    @GetMapping("/color/edit/{id}")
    public String showEditForm(@PathVariable Integer id) {
        return "redirect:/admin/color?editId=" + id;
    }

    @GetMapping("/color/delete/{id}")
    public String deleteColor(@PathVariable Integer id) {
        mauSacService.deleteById(id);
        return "redirect:/admin/color";
    }
}
