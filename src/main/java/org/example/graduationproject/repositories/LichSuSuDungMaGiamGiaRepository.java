package org.example.graduationproject.repositories;

import org.example.graduationproject.models.LichSuSuDungMaGiamGia;
import org.example.graduationproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichSuSuDungMaGiamGiaRepository extends JpaRepository<LichSuSuDungMaGiamGia, Long> {
    
    /**
     * Kiểm tra xem user đã sử dụng mã giảm giá này chưa
     */
    @Query("SELECT COUNT(l) > 0 FROM LichSuSuDungMaGiamGia l WHERE l.user = :user AND l.maGiamGiaCode = :maGiamGiaCode AND l.trangThai = 'USED'")
    boolean existsByUserAndMaGiamGiaCode(@Param("user") User user, @Param("maGiamGiaCode") String maGiamGiaCode);
    
    /**
     * Lấy danh sách mã giảm giá đã sử dụng bởi user
     */
    @Query("SELECT l.maGiamGiaCode FROM LichSuSuDungMaGiamGia l WHERE l.user = :user AND l.trangThai = 'USED'")
    List<String> findUsedMaGiamGiaCodesByUser(@Param("user") User user);
    
    /**
     * Lấy lịch sử sử dụng mã giảm giá của user
     */
    @Query("SELECT l FROM LichSuSuDungMaGiamGia l WHERE l.user = :user ORDER BY l.thoiGianSuDung DESC")
    List<LichSuSuDungMaGiamGia> findByUserOrderByThoiGianSuDungDesc(@Param("user") User user);
    
    /**
     * Kiểm tra xem có tồn tại lịch sử sử dụng mã giảm giá cho đơn hàng cụ thể không
     */
    @Query("SELECT l FROM LichSuSuDungMaGiamGia l WHERE l.user = :user AND l.donHangId = :donHangId")
    List<LichSuSuDungMaGiamGia> findByUserAndDonHangId(@Param("user") User user, @Param("donHangId") Long donHangId);
}

