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
}
