package org.example.graduationproject.controllers.admin;


import org.example.graduationproject.models.Loai;
import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.NhaCungCapRepository;
import org.example.graduationproject.repositories.ImageSanPhamRepository;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    @Autowired
    private LoaiRepository loaiRepository;

    @Autowired
    private NhanHieuRepository nhanHieuRepository;

    @Autowired
    private NhaCungCapRepository nhaCungCapRepository;

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private ImageSanPhamRepository imageSanPhamRepository;


    @GetMapping("/product")
    public String productPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product");
        model.addAttribute("products", sanPhamService.getAll());

        return "admin/product";
    }

    @GetMapping("/productForm")
    public String productForm(Model model) {
        ProductDTO productDTO = new ProductDTO();
        model.addAttribute("product", productDTO);
        List<Loai> loais = loaiRepository.findAll();
        model.addAttribute("loais", loais);
        List<NhanHieu> nhanHieus = nhanHieuRepository.findAll();
        model.addAttribute("nhanHieus", nhanHieus);
        List<NhaCungCap> nhaCungCaps = nhaCungCapRepository.findAll();
        model.addAttribute("nhaCungCaps", nhaCungCaps);
        return "admin/form-product";
    }

    @PostMapping("/productForm")
    public String addProduct(@ModelAttribute ProductDTO productDTO, @RequestParam("imageUrls") String imageUrls, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.saveProductWithUrls(productDTO, imageUrls);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }
        return "redirect:/admin/product";
    }

    @PostMapping("/product/toggle-active/{id}")
    @ResponseBody
    public void toggleActiveStatus(@PathVariable("id") Integer id, @RequestBody java.util.Map<String, Boolean> body) {
        boolean active = body.get("active");
        sanPhamService.updateActiveStatus(id, active);
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error while deleting product: " + e.getMessage());
        }
        return "redirect:/admin/product";
    }
}
