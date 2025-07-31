package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.dto.ProductVariantDTO;
import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.*;
import org.example.graduationproject.services.SanPhamBienTheService;
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

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ProductVariantController {

    @Autowired
    private SanPhamBienTheService sanPhamBienTheService;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @GetMapping("/product-variant")
    public String productVariant(Model model,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product-variant");

        Page<SanPhamBienThe> variantPage;
        if (search != null && !search.trim().isEmpty()) {
            variantPage = sanPhamBienTheService.searchByKeywordPaging(search, PageRequest.of(page, size));
            model.addAttribute("search", search);
        } else {
            variantPage = sanPhamBienTheService.getAllPaging(PageRequest.of(page, size));
        }

        model.addAttribute("variants", variantPage.getContent());
        model.addAttribute("currentPageNumber", page);
        model.addAttribute("totalPages", variantPage.getTotalPages());
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
        List<MauSac> colors = mauSacRepository.findAll();
        List<Size> sizes = sizeRepository.findAll();

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
            SanPham sanPham = sanPhamRepository.findById(productVariantDTO.getSanPhamId()).orElse(null);
            MauSac mauSac = mauSacRepository.findById(productVariantDTO.getMauSacId()).orElse(null);
            Size size = sizeRepository.findById(productVariantDTO.getSizeId()).orElse(null);

            if (sanPham == null || mauSac == null || size == null) {
                redirectAttributes.addFlashAttribute("error", "Thông tin sản phẩm, màu sắc hoặc size không hợp lệ!");
                return "redirect:/admin/product-variant/add";
            }

            SanPhamBienThe sanPhamBienThe = new SanPhamBienThe();
            sanPhamBienThe.setSoLuongTon(productVariantDTO.getSoLuongTon());
            sanPhamBienThe.setSanPham(sanPham);
            sanPhamBienThe.setMauSac(mauSac);
            sanPhamBienThe.setSize(size);

            sanPhamBienTheService.saveSanPhamBienThe(sanPhamBienThe);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }

    @GetMapping("/product-variant/edit/{id}")
    public String editProductVariantForm(@PathVariable("id") Integer id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product-variant");

        SanPhamBienThe sanPhamBienThe = sanPhamBienTheService.getSanPhamBienTheById(id);
        if (sanPhamBienThe == null) {
            return "redirect:/admin/product-variant";
        }

        ProductVariantDTO productVariantDTO = new ProductVariantDTO();
        productVariantDTO.setId(sanPhamBienThe.getId());
        productVariantDTO.setSoLuongTon(sanPhamBienThe.getSoLuongTon());
        productVariantDTO.setSanPhamId(sanPhamBienThe.getSanPham().getId());
        productVariantDTO.setSanPhamTen(sanPhamBienThe.getSanPham().getTen());
        productVariantDTO.setMauSacId(sanPhamBienThe.getMauSac().getId());
        productVariantDTO.setMauSacTen(sanPhamBienThe.getMauSac().getMaMau());
        productVariantDTO.setSizeId(sanPhamBienThe.getSize().getId());
        productVariantDTO.setSizeTen(sanPhamBienThe.getSize().getTenSize());

        List<SanPham> products = sanPhamRepository.findAll();
        List<MauSac> colors = mauSacRepository.findAll();
        List<Size> sizes = sizeRepository.findAll();

        model.addAttribute("products", products);
        model.addAttribute("colors", colors);
        model.addAttribute("sizes", sizes);
        model.addAttribute("productVariantDTO", productVariantDTO);

        return "admin/form-product-variant";
    }

    @PostMapping("/product-variant/edit")
    public String editProductVariant(@ModelAttribute ProductVariantDTO productVariantDTO, RedirectAttributes redirectAttributes) {
        try {
            SanPhamBienThe sanPhamBienThe = sanPhamBienTheService.getSanPhamBienTheById(productVariantDTO.getId());
            if (sanPhamBienThe == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm biến thể!");
                return "redirect:/admin/product-variant";
            }

            SanPham sanPham = sanPhamRepository.findById(productVariantDTO.getSanPhamId()).orElse(null);
            MauSac mauSac = mauSacRepository.findById(productVariantDTO.getMauSacId()).orElse(null);
            Size size = sizeRepository.findById(productVariantDTO.getSizeId()).orElse(null);

            if (sanPham == null || mauSac == null || size == null) {
                redirectAttributes.addFlashAttribute("error", "Thông tin sản phẩm, màu sắc hoặc size không hợp lệ!");
                return "redirect:/admin/product-variant/edit/" + productVariantDTO.getId();
            }

            sanPhamBienThe.setSoLuongTon(productVariantDTO.getSoLuongTon());
            sanPhamBienThe.setSanPham(sanPham);
            sanPhamBienThe.setMauSac(mauSac);
            sanPhamBienThe.setSize(size);

            sanPhamBienTheService.saveSanPhamBienThe(sanPhamBienThe);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }

    @GetMapping("/product-variant/delete/{id}")
    public String deleteProductVariant(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamBienTheService.deleteSanPhamBienThe(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/product-variant";
    }
}
