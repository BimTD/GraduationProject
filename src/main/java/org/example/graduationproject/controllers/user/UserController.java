package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.services.LoaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/home")
public class UserController extends BaseController {

    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private LoaiService loaiService;

    @GetMapping
    public String homePage(Model model) {
        // Lấy tất cả loại sản phẩm
        List<Loai> allLoai = loaiService.getAllLoai();

        // Lấy sản phẩm theo giới tính và loại cho từng tab
        Map<String, List<SanPham>> sanPhamNamTheoLoai = new HashMap<>();
        Map<String, List<SanPham>> sanPhamNuTheoLoai = new HashMap<>();
        
        for (Loai loai : allLoai) {
            String tenLoai = loai.getTen().toLowerCase();
            
            // Lấy sản phẩm nam theo loại (giới tính = 1)
            List<SanPham> spNamTheoLoai = sanPhamService.filterByCategoryAndGenderPaging(loai.getId(), 1, 0, 100).getContent();
            sanPhamNamTheoLoai.put(tenLoai, spNamTheoLoai);
            
            // Lấy sản phẩm nữ theo loại (giới tính = 2)
            List<SanPham> spNuTheoLoai = sanPhamService.filterByCategoryAndGenderPaging(loai.getId(), 2, 0, 100).getContent();
            sanPhamNuTheoLoai.put(tenLoai, spNuTheoLoai);
        }
        
        model.addAttribute("sanPhamNamTheoLoai", sanPhamNamTheoLoai);
        model.addAttribute("sanPhamNuTheoLoai", sanPhamNuTheoLoai);
        
        // Cũng cung cấp danh sách sản phẩm đơn giản
        List<SanPham> products = sanPhamService.getAll();
        model.addAttribute("products", products);
        
        return "user/home";
    }
}
