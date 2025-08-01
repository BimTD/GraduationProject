package org.example.graduationproject.controllers.admin;


import org.example.graduationproject.models.Loai;
import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.NhaCungCapRepository;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import org.example.graduationproject.models.SanPham;

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

    @GetMapping("/product")
    public String productPage(Model model,
                              @RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "categoryId", required = false) Integer categoryId,
                              @RequestParam(value = "gender", required = false) String gender,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product");
        
        // Lấy danh sách categories cho filter
        List<Loai> categories = loaiRepository.findAll();
        model.addAttribute("categories", categories);
        
        Page<SanPham> productPage = sanPhamService.getProductsWithFilters(search, categoryId, gender, page, size);
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        int lastPage = productPage.getTotalPages() > 0 ? productPage.getTotalPages() - 1 : 0;
        int prevPage = page > 0 ? page - 1 : 0;
        int nextPage = (page + 1 < productPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        
        // Thêm các filter values vào model
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("gender", gender);

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
        model.addAttribute("editMode", false);
        return "admin/form-product";
    }

    @GetMapping("/product/edit/{id}")
    public String editProductForm(@PathVariable("id") Integer id, Model model) {
        Optional<ProductDTO> productDTOOpt = sanPhamService.getProductDTOById(id);
        if (productDTOOpt.isEmpty()) {
            return "redirect:/admin/product";
        }
        
        Optional<SanPham> sanPhamOpt = sanPhamService.findById(id);
        if (sanPhamOpt.isEmpty()) {
            return "redirect:/admin/product";
        }
        
        model.addAttribute("product", productDTOOpt.get());
        model.addAttribute("editMode", true);
        model.addAttribute("existingImages", sanPhamOpt.get().getImages());
        
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

    @PostMapping("/product/update")
    public String updateProduct(@ModelAttribute ProductDTO productDTO, @RequestParam("imageUrls") String imageUrls, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.updateProductWithUrls(productDTO, imageUrls);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }
        return "redirect:/admin/product";
    }

    @PostMapping("/product/delete-image/{imageId}")
    @ResponseBody
    public String deleteImage(@PathVariable("imageId") Integer imageId) {
        try {
            sanPhamService.deleteImageById(imageId);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/product/toggle-active/{id}")
    @ResponseBody
    public void toggleActiveStatus(@PathVariable("id") Integer id, @RequestBody java.util.Map<String, Boolean> body) {
        boolean active = body.get("active");
        sanPhamService.toggleProductActiveStatus(id, active);
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error while deleting product: " + e.getMessage());
        }
        return "redirect:/admin/product";
    }
}
