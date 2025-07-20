package org.example.graduationproject.controllers.admin;


import org.example.graduationproject.models.Loai;
import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.ImageSanPham;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.NhaCungCapRepository;
import org.example.graduationproject.repositories.ImageSanPhamRepository;
import org.example.graduationproject.services.LoaiService;
import org.example.graduationproject.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    // Thư mục lưu ảnh
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/products/";


    @GetMapping("/product")
    public String productPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "product");

        return "admin/product";
    }

    @GetMapping("/productForm")
    public String productForm(Model model) {

        SanPham sanPham = new SanPham();
        model.addAttribute("product", sanPham);

        List<Loai> loais = loaiRepository.findAll();
        model.addAttribute("loais", loais);

        List<NhanHieu> nhanHieus = nhanHieuRepository.findAll();
        model.addAttribute("nhanHieus", nhanHieus);
        
        List<NhaCungCap> nhaCungCaps = nhaCungCapRepository.findAll();
        model.addAttribute("nhaCungCaps", nhaCungCaps);

        return "admin/form-product";
    }

    @PostMapping("/productForm")
    public String addProduct() {
        return null;
    }

}
