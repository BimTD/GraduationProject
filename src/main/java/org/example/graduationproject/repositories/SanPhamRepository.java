package org.example.graduationproject.repositories;

import org.example.graduationproject.models.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    List<SanPham> findByGioiTinh(Integer gioiTinh);
    
    Page<SanPham> findAll(Pageable pageable);
    
    Page<SanPham> findByTenContainingIgnoreCase(String ten, Pageable pageable);
    
    // Filter by category
    Page<SanPham> findByLoai_Id(Integer loaiId, Pageable pageable);
    
    // Filter by gender
    Page<SanPham> findByGioiTinh(Integer gioiTinh, Pageable pageable);
    
    // Filter by category and gender
    Page<SanPham> findByLoai_IdAndGioiTinh(Integer loaiId, Integer gioiTinh, Pageable pageable);
    
    // Search by name and filter by category
    Page<SanPham> findByTenContainingIgnoreCaseAndLoai_Id(String ten, Integer loaiId, Pageable pageable);
    
    // Search by name and filter by gender
    Page<SanPham> findByTenContainingIgnoreCaseAndGioiTinh(String ten, Integer gioiTinh, Pageable pageable);
    
    // Search by name and filter by category and gender
    Page<SanPham> findByTenContainingIgnoreCaseAndLoai_IdAndGioiTinh(String ten, Integer loaiId, Integer gioiTinh, Pageable pageable);
    
    // Get related products (same category and gender, excluding current product)
    Page<SanPham> findByLoai_IdAndGioiTinhAndIdNotAndTrangThaiHoatDongTrue(Integer loaiId, Integer gioiTinh, Integer excludeId, Pageable pageable);
    
    // Upsell products: same brand, different category, with discount
    Page<SanPham> findByNhanHieu_IdAndIdNotAndTrangThaiHoatDongTrueAndKhuyenMaiGreaterThanOrderByKhuyenMaiDesc(
            Integer nhanHieuId, Integer excludeId, BigDecimal minDiscount, Pageable pageable);
    
    // Alternative upsell: higher price products in same category
    Page<SanPham> findByLoai_IdAndGioiTinhAndIdNotAndTrangThaiHoatDongTrueAndGiaBanGreaterThanOrderByGiaBanDesc(
        Integer loaiId, Integer gioiTinh, Integer excludeId, BigDecimal minPrice, Pageable pageable);
    
    // Search methods for modern search functionality - CHỈ TÌM THEO TÊN SẢN PHẨM
    @Query("SELECT s FROM SanPham s WHERE " +
           "LOWER(s.ten) LIKE LOWER(:searchTerm) AND " +
           "s.trangThaiHoatDong = true " +
           "ORDER BY " +
           "CASE WHEN LOWER(s.ten) LIKE LOWER(:exactTerm) THEN 1 " +
           "WHEN LOWER(s.ten) LIKE LOWER(:startTerm) THEN 2 " +
           "WHEN LOWER(s.ten) LIKE LOWER(:searchTerm) THEN 3 " +
           "ELSE 4 END, s.ngayTao DESC")
    List<SanPham> findTopSuggestions(@Param("searchTerm") String searchTerm, 
                                   @Param("exactTerm") String exactTerm,
                                   @Param("startTerm") String startTerm,
                                   Pageable pageable);
    
    @Query("SELECT s FROM SanPham s WHERE " +
           "LOWER(s.ten) LIKE LOWER(:searchTerm) AND " +
           "s.trangThaiHoatDong = true " +
           "ORDER BY " +
           "CASE WHEN LOWER(s.ten) LIKE LOWER(:exactTerm) THEN 1 " +
           "WHEN LOWER(s.ten) LIKE LOWER(:startTerm) THEN 2 " +
           "WHEN LOWER(s.ten) LIKE LOWER(:searchTerm) THEN 3 " +
           "ELSE 4 END, s.ngayTao DESC")
    Page<SanPham> findBySearchTerm(@Param("searchTerm") String searchTerm,
                                 @Param("exactTerm") String exactTerm,
                                 @Param("startTerm") String startTerm,
                                 Pageable pageable);
    
    @Query("SELECT s FROM SanPham s WHERE " +
           "LOWER(s.ten) LIKE LOWER(:searchTerm) AND " +
           "s.loai.id = :categoryId AND " +
           "s.trangThaiHoatDong = true " +
           "ORDER BY " +
           "CASE WHEN LOWER(s.ten) LIKE LOWER(:exactTerm) THEN 1 " +
           "WHEN LOWER(s.ten) LIKE LOWER(:startTerm) THEN 2 " +
           "WHEN LOWER(s.ten) LIKE LOWER(:searchTerm) THEN 3 " +
           "ELSE 4 END, s.ngayTao DESC")
    Page<SanPham> findBySearchTermAndCategory(@Param("searchTerm") String searchTerm,
                                            @Param("exactTerm") String exactTerm,
                                            @Param("startTerm") String startTerm,
                                            @Param("categoryId") Integer categoryId, 
                                            Pageable pageable);
    
    // Methods for ProductInfoService
    List<SanPham> findByLoaiTenAndTrangThaiHoatDong(String tenLoai, Boolean trangThaiHoatDong);
    long countByTrangThaiHoatDong(Boolean trangThaiHoatDong);
    List<SanPham> findByTenContainingIgnoreCaseAndTrangThaiHoatDong(String ten, Boolean trangThaiHoatDong);
    
    // Methods for price-based search
    List<SanPham> findByGiaBanBetweenAndTrangThaiHoatDong(BigDecimal minPrice, BigDecimal maxPrice, Boolean trangThaiHoatDong);
    List<SanPham> findByGiaBanGreaterThanEqualAndTrangThaiHoatDong(BigDecimal minPrice, Boolean trangThaiHoatDong);
    List<SanPham> findByGiaBanLessThanEqualAndTrangThaiHoatDong(BigDecimal maxPrice, Boolean trangThaiHoatDong);
    
    // Methods for checking product name uniqueness
    boolean existsByTenIgnoreCase(String ten);
    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
    
    // Methods for filtering products by category and status
    Page<SanPham> findByLoai_IdAndTrangThaiHoatDongTrue(Integer loaiId, Pageable pageable);
    
    // Method for getting products by supplier
    List<SanPham> findByNhaCungCapId(Integer nhaCungCapId);
    
    // Method for getting newest products
    Page<SanPham> findByTrangThaiHoatDongTrueOrderByNgayTaoDesc(Pageable pageable);
    List<SanPham> findTop6ByTrangThaiHoatDongTrueOrderByNgayTaoDesc();
    
    // Methods for chatbot intelligent search
    List<SanPham> findByNhanHieuTenContainingIgnoreCaseAndTrangThaiHoatDong(String brandName, Boolean trangThaiHoatDong);
    List<SanPham> findByBienThesMauSacMaMauContainingIgnoreCaseAndTrangThaiHoatDong(String colorName, Boolean trangThaiHoatDong);
    List<SanPham> findByBienThesSizeTenSizeContainingIgnoreCaseAndTrangThaiHoatDong(String sizeName, Boolean trangThaiHoatDong);
    List<SanPham> findByTagContainingIgnoreCaseAndTrangThaiHoatDong(String tagName, Boolean trangThaiHoatDong);
    List<SanPham> findByGioiTinhAndTrangThaiHoatDong(Integer gioiTinh, Boolean trangThaiHoatDong);
    List<SanPham> findByTrangThaiHoatDongTrueAndKhuyenMaiGreaterThanOrderByKhuyenMaiDesc(BigDecimal minDiscount);
    
    // Count methods for statistics
    long countByLoaiTenAndTrangThaiHoatDong(String tenLoai, Boolean trangThaiHoatDong);
    long countByNhanHieuTenAndTrangThaiHoatDong(String tenNhanHieu, Boolean trangThaiHoatDong);
}
