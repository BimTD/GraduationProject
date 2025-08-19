package org.example.graduationproject.repositories;

import org.example.graduationproject.models.SanPhamBienThe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamBienTheRepository extends JpaRepository<SanPhamBienThe, Integer> {
    
    List<SanPhamBienThe> findBySanPhamId(Integer sanPhamId);
    
    @Query("SELECT spbt FROM SanPhamBienThe spbt WHERE spbt.sanPham.ten LIKE %:search% OR spbt.mauSac.maMau LIKE %:search% OR spbt.size.tenSize LIKE %:search% OR spbt.sanPham.loai.ten LIKE %:search%")
    List<SanPhamBienThe> searchByKeyword(@Param("search") String search);

    @Query("SELECT spbt FROM SanPhamBienThe spbt WHERE spbt.sanPham.ten LIKE %:search% OR spbt.mauSac.maMau LIKE %:search% OR spbt.size.tenSize LIKE %:search% OR spbt.sanPham.loai.ten LIKE %:search%")
    Page<SanPhamBienThe> searchByKeywordPaging(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT spbt FROM SanPhamBienThe spbt WHERE spbt.sanPham.id = :sanPhamId")
    List<SanPhamBienThe> findBySanPhamIdWithDetails(@Param("sanPhamId") Integer sanPhamId);
    
    // Load SanPhamBienThe cùng với tất cả các quan hệ
    @Query("SELECT spbt FROM SanPhamBienThe spbt " +
           "LEFT JOIN FETCH spbt.sanPham s " +
           "LEFT JOIN FETCH s.loai " +
           "LEFT JOIN FETCH spbt.mauSac m " +
           "LEFT JOIN FETCH m.loai " +
           "LEFT JOIN FETCH spbt.size sz " +
           "LEFT JOIN FETCH sz.loai")
    Page<SanPhamBienThe> findAllWithDetails(Pageable pageable);
    
    @Query("SELECT spbt FROM SanPhamBienThe spbt " +
           "LEFT JOIN FETCH spbt.sanPham s " +
           "LEFT JOIN FETCH s.loai " +
           "LEFT JOIN FETCH spbt.mauSac m " +
           "LEFT JOIN FETCH m.loai " +
           "LEFT JOIN FETCH spbt.size sz " +
           "LEFT JOIN FETCH sz.loai " +
           "WHERE spbt.sanPham.ten LIKE %:search% OR spbt.mauSac.maMau LIKE %:search% OR spbt.size.tenSize LIKE %:search% OR spbt.sanPham.loai.ten LIKE %:search%")
    Page<SanPhamBienThe> searchByKeywordWithDetails(@Param("search") String search, Pageable pageable);
    
    // Phương thức mới để kiểm tra biến thể đã tồn tại
    boolean existsBySanPhamIdAndMauSacIdAndSizeId(Integer sanPhamId, Integer mauSacId, Integer sizeId);
}