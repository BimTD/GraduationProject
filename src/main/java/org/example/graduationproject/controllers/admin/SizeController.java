package org.example.graduationproject.controllers.admin;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.graduationproject.models.Size;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.repositories.SizeRepository;
import org.example.graduationproject.repositories.LoaiRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.example.graduationproject.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class SizeController {

    @Autowired
    private SizeService sizeService;
    @Autowired
    private LoaiRepository loaiRepository;

    @GetMapping("/size")
    public String sizePage(@RequestParam(value = "editId", required = false) Integer editId,
                          @RequestParam(value = "loaiId", required = false) Integer loaiId,
                          Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "size");
        List<Loai> loais = loaiRepository.findAll();
        model.addAttribute("loais", loais);
        List<Size> sizes = sizeService.findAll();
        model.addAttribute("sizes", sizes);
        boolean editMode = false;
        Size sizeForm = new Size();
        String formAction = "/admin/size/add";
        if (editId != null) {
            Optional<Size> editSize = sizeService.findById(editId);
            if (editSize.isPresent()) {
                sizeForm = editSize.get();
                editMode = true;
                formAction = "/admin/size/edit";
            }
        }
        model.addAttribute("size", sizeForm);
        model.addAttribute("editMode", editMode);
        model.addAttribute("formAction", formAction);
        return "admin/size";
    }

    @PostMapping("/size/add")
    public String addSize(@ModelAttribute Size size, RedirectAttributes redirectAttributes) {
        if (size.getLoai() != null && size.getLoai().getId() != null) {
            sizeService.save(size);
        }
        return "redirect:/admin/size";
    }

    @PostMapping("/size/edit")
    public String editSize(@ModelAttribute Size size, RedirectAttributes redirectAttributes) {
        if (size.getId() != null && size.getLoai() != null && size.getLoai().getId() != null) {
            sizeService.save(size);
        }
        return "redirect:/admin/size";
    }

    @GetMapping("/size/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        return "redirect:/admin/size?editId=" + id;
    }

    @GetMapping("/size/delete/{id}")
    public String deleteSize(@PathVariable Integer id) {
        sizeService.deleteById(id);
        return "redirect:/admin/size";
    }
}
