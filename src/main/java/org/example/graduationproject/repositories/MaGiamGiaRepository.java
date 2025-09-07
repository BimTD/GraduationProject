package org.example.graduationproject.repositories;

import org.example.graduationproject.models.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {
    
    // Tìm mã giảm giá theo mã code
    Optional<MaGiamGia> findByMaGiamGia(String maGiamGia);
    
    // Tìm mã giảm giá đang hoạt động theo mã code
    @Query("SELECT m FROM MaGiamGia m WHERE m.maGiamGia = :maGiamGia AND m.trangThai = 'ACTIVE' " +
           "AND :now BETWEEN m.ngayBatDau AND m.ngayKetThuc " +
           "AND (m.soLuongSuDung IS NULL OR m.soLuongDaSuDung < m.soLuongSuDung)")
    Optional<MaGiamGia> findActiveByMaGiamGia(@Param("maGiamGia") String maGiamGia, @Param("now") LocalDateTime now);
    
    // Tìm tất cả mã giảm giá đang hoạt động
    @Query("SELECT m FROM MaGiamGia m WHERE m.trangThai = 'ACTIVE' " +
           "AND :now BETWEEN m.ngayBatDau AND m.ngayKetThuc " +
           "AND (m.soLuongSuDung IS NULL OR m.soLuongDaSuDung < m.soLuongSuDung)")
    List<MaGiamGia> findAllActive(@Param("now") LocalDateTime now);
    
    // Tìm mã giảm giá theo trạng thái
    List<MaGiamGia> findByTrangThai(String trangThai);
    
    // Tìm mã giảm giá hết hạn
    @Query("SELECT m FROM MaGiamGia m WHERE m.ngayKetThuc < :now AND m.trangThai = 'ACTIVE'")
    List<MaGiamGia> findExpiredCodes(@Param("now") LocalDateTime now);
    
    // Tìm mã giảm giá hết số lượng sử dụng
    @Query("SELECT m FROM MaGiamGia m WHERE m.soLuongSuDung IS NOT NULL " +
           "AND m.soLuongDaSuDung >= m.soLuongSuDung AND m.trangThai = 'ACTIVE'")
    List<MaGiamGia> findExhaustedCodes();
    
    // Kiểm tra mã giảm giá có tồn tại và đang hoạt động không
    @Query("SELECT COUNT(m) > 0 FROM MaGiamGia m WHERE m.maGiamGia = :maGiamGia AND m.trangThai = 'ACTIVE' " +
           "AND :now BETWEEN m.ngayBatDau AND m.ngayKetThuc " +
           "AND (m.soLuongSuDung IS NULL OR m.soLuongDaSuDung < m.soLuongSuDung)")
    boolean existsActiveByMaGiamGia(@Param("maGiamGia") String maGiamGia, @Param("now") LocalDateTime now);
    
    // Tìm mã giảm giá có thể áp dụng cho sản phẩm cụ thể
    @Query("SELECT DISTINCT m FROM MaGiamGia m LEFT JOIN m.sanPhams s " +
           "WHERE m.trangThai = 'ACTIVE' " +
           "AND :now BETWEEN m.ngayBatDau AND m.ngayKetThuc " +
           "AND (m.soLuongSuDung IS NULL OR m.soLuongDaSuDung < m.soLuongSuDung) " +
           "AND (m.apDungChoTatCa = true OR s.id = :sanPhamId)")
    List<MaGiamGia> findApplicableForProduct(@Param("sanPhamId") Integer sanPhamId, @Param("now") LocalDateTime now);
    
    // Tìm mã giảm giá theo khoảng thời gian
    @Query("SELECT m FROM MaGiamGia m WHERE m.ngayTao BETWEEN :startDate AND :endDate")
    List<MaGiamGia> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
