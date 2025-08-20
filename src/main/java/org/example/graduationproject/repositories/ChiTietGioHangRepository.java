package org.example.graduationproject.repositories;

import org.example.graduationproject.models.ChiTietGioHang;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.SanPhamBienThe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Integer> {
    Optional<ChiTietGioHang> findByGioHangAndSanPhamBienThe(GioHang gioHang, SanPhamBienThe sanPhamBienThe);
}
