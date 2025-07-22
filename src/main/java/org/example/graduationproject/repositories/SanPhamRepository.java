package org.example.graduationproject.repositories;

import org.example.graduationproject.models.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    List<SanPham> findByGioiTinh(Integer gioiTinh);
}
