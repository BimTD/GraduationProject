package org.example.graduationproject.services;

import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SanPhamService {
    List<SanPham> getAll();

    Boolean create(SanPham sanPham);
    SanPham findById(Integer id);
    Boolean deleteById(Integer id);
    Boolean update(SanPham sanPham);
    SanPham save(SanPham sanPham);
    void saveProductWithUrls(ProductDTO productDTO, String imageUrls) throws Exception;
}