package org.example.graduationproject.services;

import org.example.graduationproject.models.ChiTietPhieuNhapHang;
import java.util.List;
import java.util.Optional;

public interface ChiTietPhieuNhapHangService {
    List<ChiTietPhieuNhapHang> getAll();
    Optional<ChiTietPhieuNhapHang> findById(Integer id);
    ChiTietPhieuNhapHang save(ChiTietPhieuNhapHang chiTietPhieuNhapHang);
    void deleteById(Integer id);
    List<ChiTietPhieuNhapHang> findByPhieuNhapHangId(Integer phieuNhapHangId);
} 