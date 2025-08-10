package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.PhieuNhapHang;
import org.example.graduationproject.repositories.PhieuNhapHangRepository;
import org.example.graduationproject.services.PhieuNhapHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PhieuNhapHangServiceImpl implements PhieuNhapHangService {
    
    @Autowired
    private PhieuNhapHangRepository phieuNhapHangRepository;
    
    @Override
    public List<PhieuNhapHang> getAll() {
        return phieuNhapHangRepository.findAll();
    }
    
    @Override
    public Optional<PhieuNhapHang> findById(Integer id) {
        return phieuNhapHangRepository.findById(id);
    }
    
    @Override
    public PhieuNhapHang save(PhieuNhapHang phieuNhapHang) {
        return phieuNhapHangRepository.save(phieuNhapHang);
    }
    
    @Override
    public void deleteById(Integer id) {
        phieuNhapHangRepository.deleteById(id);
    }
    
    @Override
    public Page<PhieuNhapHang> getAllPaging(Pageable pageable) {
        return phieuNhapHangRepository.findAll(pageable);
    }
    
    @Override
    public Page<PhieuNhapHang> searchBySoChungTuContainingIgnoreCase(String soChungTu, Pageable pageable) {
        return phieuNhapHangRepository.findBySoChungTuContainingIgnoreCase(soChungTu, pageable);
    }
    
    @Override
    public Page<PhieuNhapHang> searchByNhaCungCapTenContainingIgnoreCase(String tenNhaCungCap, Pageable pageable) {
        return phieuNhapHangRepository.findByNhaCungCap_TenContainingIgnoreCase(tenNhaCungCap, pageable);
    }
} 