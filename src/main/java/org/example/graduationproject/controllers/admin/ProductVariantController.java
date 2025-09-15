package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.dto.BulkProductVariantDTO;
import org.example.graduationproject.dto.ProductVariantDTO;
import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.services.MauSacService;
import org.example.graduationproject.services.SanPhamBienTheService;
import org.example.graduationproject.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ProductVariantController {

    @Autowired
    private SanPhamBienTheService sanPhamBienTheService;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private MauSacService mauSacService;

    @Autowired
    private SizeService sizeService;

    @GetMapping("/product-variant")
    public String productVariant(Model model,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product-variant");

        if (page < 0) {
            page = 0;
        }

        Page<SanPhamBienThe> variantPage;
        if (search != null && !search.trim().isEmpty()) {
            variantPage = sanPhamBienTheService.searchByKeywordPagingWithDetails(search, PageRequest.of(page, size));
            model.addAttribute("search", search);
        } else {
            variantPage = sanPhamBienTheService.getAllPagingWithDetails(PageRequest.of(page, size));
        }

        int totalPages = variantPage.getTotalPages();
        if (page >= totalPages && totalPages > 0) {
            return "redirect:/admin/product-variant?page=" + (totalPages - 1) + 
                   (search != null && !search.trim().isEmpty() ? "&search=" + search : "") +
                   "&size=" + size;
        }

        model.addAttribute("variants", variantPage.getContent());
        model.addAttribute("currentPageNumber", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", variantPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/product-variant";
    }

    @GetMapping("/product-variant/add")
    public String addProductVariantForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product-variant");

        List<SanPham> products = sanPhamRepository.findAll();
        List<MauSac> colors = mauSacService.findAllWithCategory();
        List<Size> sizes = sizeService.findAllWithCategory();

        ProductVariantDTO productVariantDTO = new ProductVariantDTO();
        productVariantDTO.setSoLuongTon(0);
        
        model.addAttribute("products", products);
        model.addAttribute("colors", colors);
        model.addAttribute("sizes", sizes);
        model.addAttribute("productVariantDTO", productVariantDTO);

        return "admin/form-product-variant";
    }

    @PostMapping("/product-variant/add")
    public String addProductVariant(@ModelAttribute ProductVariantDTO productVariantDTO, RedirectAttributes redirectAttributes) {
        try {
            sanPhamBienTheService.createProductVariant(productVariantDTO);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }

    @GetMapping("/product-variant/edit/{id}")
    public String editProductVariantForm(@PathVariable("id") Integer id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product-variant");

        Optional<ProductVariantDTO> productVariantDTOOpt = sanPhamBienTheService.getProductVariantDTOById(id);
        if (productVariantDTOOpt.isEmpty()) {
            return "redirect:/admin/product-variant";
        }

        List<SanPham> products = sanPhamRepository.findAll();
        List<MauSac> colors = mauSacService.findAllWithCategory();
        List<Size> sizes = sizeService.findAllWithCategory();

        model.addAttribute("products", products);
        model.addAttribute("colors", colors);
        model.addAttribute("sizes", sizes);
        model.addAttribute("productVariantDTO", productVariantDTOOpt.get());

        return "admin/form-product-variant";
    }

    @PostMapping("/product-variant/edit")
    public String editProductVariant(@ModelAttribute ProductVariantDTO productVariantDTO, RedirectAttributes redirectAttributes) {
        try {
            sanPhamBienTheService.updateProductVariant(productVariantDTO);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật phiên bản sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }

    @GetMapping("/product-variant/delete/{id}")
    public String deleteProductVariant(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamBienTheService.deleteProductVariantById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa thành công sản phẩm biến thể!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }
    
    // Thêm endpoint mới để hiển thị form tạo biến thể hàng loạt
    @GetMapping("/product-variant/bulk-add")
    public String bulkAddProductVariantForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product-variant");

        List<SanPham> products = sanPhamRepository.findAll();

        BulkProductVariantDTO bulkDTO = new BulkProductVariantDTO();
        bulkDTO.setSoLuongTon(0);
        
        model.addAttribute("products", products);
        model.addAttribute("bulkDTO", bulkDTO);

        return "admin/bulk-product-variant";
    }
    
    // Endpoint mới để lấy màu sắc và size phù hợp với sản phẩm (AJAX)
    @GetMapping("/product-variant/get-available-options/{sanPhamId}")
    @ResponseBody
    public BulkProductVariantDTO getAvailableOptions(@PathVariable Integer sanPhamId) {
        return sanPhamBienTheService.getAvailableColorsAndSizesForProduct(sanPhamId);
    }
    
    // Endpoint để xử lý tạo biến thể hàng loạt
    @PostMapping("/product-variant/bulk-add")
    public String bulkAddProductVariant(@ModelAttribute BulkProductVariantDTO bulkDTO, RedirectAttributes redirectAttributes) {
        try {
            BulkProductVariantDTO.BulkCreateResult result = sanPhamBienTheService.createBulkProductVariantsWithResult(bulkDTO);
            
            // Tạo thông báo chi tiết
            StringBuilder successMessage = new StringBuilder();
            successMessage.append(result.getMessage()).append("\n");
            
            if (result.getCreatedNew() > 0) {
                successMessage.append("\n✅ Biến thể được tạo mới:\n");
                for (String variant : result.getCreatedVariants()) {
                    successMessage.append("• ").append(variant).append("\n");
                }
            }
            
            if (result.getAlreadyExists() > 0) {
                successMessage.append("\n⚠️ Biến thể đã tồn tại:\n");
                for (String variant : result.getExistingVariants()) {
                    successMessage.append("• ").append(variant).append("\n");
                }
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage.toString());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }
}
