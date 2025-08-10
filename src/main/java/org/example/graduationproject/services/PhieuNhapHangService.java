package org.example.graduationproject.services;

import org.example.graduationproject.models.PhieuNhapHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PhieuNhapHangService {
    List<PhieuNhapHang> getAll();
    Optional<PhieuNhapHang> findById(Integer id);
    PhieuNhapHang save(PhieuNhapHang phieuNhapHang);
    void deleteById(Integer id);
    
    // Thêm method phân trang và tìm kiếm
    Page<PhieuNhapHang> getAllPaging(Pageable pageable);
    Page<PhieuNhapHang> searchBySoChungTuContainingIgnoreCase(String soChungTu, Pageable pageable);
    Page<PhieuNhapHang> searchByNhaCungCapTenContainingIgnoreCase(String tenNhaCungCap, Pageable pageable);
} 