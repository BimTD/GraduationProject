package org.example.graduationproject.repositories;

import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    
    // Tìm tất cả hóa đơn của một user
    List<HoaDon> findByUserOrderByNgayTaoDesc(User user);
    
    // Tìm hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(String trangThai);
    
    // Tìm hóa đơn của user theo trạng thái
    List<HoaDon> findByUserAndTrangThaiOrderByNgayTaoDesc(User user, String trangThai);
}
