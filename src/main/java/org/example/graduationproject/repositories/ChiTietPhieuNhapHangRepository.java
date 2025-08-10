package org.example.graduationproject.repositories;

import org.example.graduationproject.models.ChiTietPhieuNhapHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietPhieuNhapHangRepository extends JpaRepository<ChiTietPhieuNhapHang, Integer> {
} 