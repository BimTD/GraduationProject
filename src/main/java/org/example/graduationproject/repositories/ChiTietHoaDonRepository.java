package org.example.graduationproject.repositories;

import org.example.graduationproject.models.ChiTietHoaDon;
import org.example.graduationproject.models.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    
    // Tìm tất cả chi tiết hóa đơn của một hóa đơn
    List<ChiTietHoaDon> findByHoaDon(HoaDon hoaDon);
    
    // Tìm chi tiết hóa đơn theo hóa đơn và sắp xếp theo ID
    List<ChiTietHoaDon> findByHoaDonOrderById(HoaDon hoaDon);
}
