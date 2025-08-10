package org.example.graduationproject.repositories;

import org.example.graduationproject.models.PhieuNhapHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuNhapHangRepository extends JpaRepository<PhieuNhapHang, Integer> {
    // Tìm kiếm theo số chứng từ
    Page<PhieuNhapHang> findBySoChungTuContainingIgnoreCase(String soChungTu, Pageable pageable);
    
    // Tìm kiếm theo tên nhà cung cấp
    Page<PhieuNhapHang> findByNhaCungCap_TenContainingIgnoreCase(String tenNhaCungCap, Pageable pageable);
} 