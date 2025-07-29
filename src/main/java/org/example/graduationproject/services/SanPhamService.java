package org.example.graduationproject.services;

import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SanPhamService {
    List<SanPham> getAll();

    List<SanPham> getByGioiTinh(Integer gioiTinh);
    Boolean create(SanPham sanPham);
    SanPham findById(Integer id);
    Boolean deleteById(Integer id);
    Boolean update(SanPham sanPham);
    SanPham save(SanPham sanPham);
    void saveProductWithUrls(ProductDTO productDTO, String imageUrls) throws Exception;
    void updateProductWithUrls(ProductDTO productDTO, String imageUrls) throws Exception;
    void updateActiveStatus(Integer id, boolean active);
    
    // Phân trang
    Page<SanPham> getAllPaging(int page, int size);
    Page<SanPham> searchByTenPaging(String ten, int page, int size);
    
    // Filter methods
    Page<SanPham> filterByCategoryPaging(Integer categoryId, int page, int size);
    Page<SanPham> filterByGenderPaging(Integer gender, int page, int size);
    Page<SanPham> filterByCategoryAndGenderPaging(Integer categoryId, Integer gender, int page, int size);
    
    // Search with filters
    Page<SanPham> searchByTenAndCategoryPaging(String ten, Integer categoryId, int page, int size);
    Page<SanPham> searchByTenAndGenderPaging(String ten, Integer gender, int page, int size);
    Page<SanPham> searchByTenAndCategoryAndGenderPaging(String ten, Integer categoryId, Integer gender, int page, int size);
}