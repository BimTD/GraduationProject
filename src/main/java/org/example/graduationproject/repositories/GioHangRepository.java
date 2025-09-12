package org.example.graduationproject.repositories;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByUserAndTrangThai(User user, String trangThai);
    Optional<GioHang> findByUserIdAndTrangThai(Long userId, String trangThai);
    long countByTrangThai(String trangThai);
    
    // Tìm giỏ hàng active cũ hơn thời gian cutoff
    @Query("SELECT g FROM GioHang g WHERE g.trangThai = 'active' AND g.ngayCapNhat < :cutoffTime")
    List<GioHang> findActiveCartsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Tìm giỏ hàng active cũ hơn thời gian cutoff (có sản phẩm)
    @Query("SELECT DISTINCT g FROM GioHang g LEFT JOIN g.chiTietGioHangs c WHERE g.trangThai = 'active' AND g.ngayCapNhat < :cutoffTime AND c IS NOT NULL")
    List<GioHang> findActiveCartsWithItemsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}
