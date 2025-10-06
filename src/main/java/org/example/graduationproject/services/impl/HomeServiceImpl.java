package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.HomeResponseDTO;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.services.HomeService;
import org.example.graduationproject.services.LoaiService;
import org.example.graduationproject.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private LoaiService loaiService;

    @Override
    public HomeResponseDTO getHomePageDataWithValidation() {
        try {
            // Lấy tất cả loại sản phẩm
            List<Loai> allLoai = loaiService.getAllLoai();

            // Lấy sản phẩm theo giới tính và loại cho từng tab
            Map<String, List<Object>> sanPhamNamTheoLoai = new HashMap<>();
            Map<String, List<Object>> sanPhamNuTheoLoai = new HashMap<>();
            
            for (Loai loai : allLoai) {
                String tenLoai = loai.getTen().toLowerCase();
                
                // Lấy sản phẩm nam theo loại (giới tính = 1)
                List<SanPham> spNamTheoLoai = sanPhamService.filterByCategoryAndGenderPaging(loai.getId(), 1, 0, 100).getContent();
                sanPhamNamTheoLoai.put(tenLoai, spNamTheoLoai.stream().map(s -> (Object) s).collect(Collectors.toList()));
                
                // Lấy sản phẩm nữ theo loại (giới tính = 2)
                List<SanPham> spNuTheoLoai = sanPhamService.filterByCategoryAndGenderPaging(loai.getId(), 2, 0, 100).getContent();
                sanPhamNuTheoLoai.put(tenLoai, spNuTheoLoai.stream().map(s -> (Object) s).collect(Collectors.toList()));
            }
            
            // Cũng cung cấp danh sách sản phẩm đơn giản
            List<SanPham> products = sanPhamService.getAll();
            List<Object> productsAsObjects = products.stream().map(s -> (Object) s).collect(Collectors.toList());
            
            // Lấy sản phẩm mới nhất (6 sản phẩm)
            List<SanPham> newestProducts = sanPhamService.getNewestProducts(6);
            List<Object> newestProductsAsObjects = newestProducts.stream().map(s -> (Object) s).collect(Collectors.toList());

            return new HomeResponseDTO(true, "Lấy dữ liệu trang chủ thành công", 
                                     sanPhamNamTheoLoai, sanPhamNuTheoLoai, productsAsObjects, newestProductsAsObjects);

        } catch (Exception e) {
            e.printStackTrace();
            return new HomeResponseDTO(false, "Có lỗi xảy ra: " + e.getMessage());
        }
    }
}


