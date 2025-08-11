package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.ChiTietPhieuNhapHang;
import org.example.graduationproject.repositories.ChiTietPhieuNhapHangRepository;
import org.example.graduationproject.services.ChiTietPhieuNhapHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChiTietPhieuNhapHangServiceImpl implements ChiTietPhieuNhapHangService {
    
    @Autowired
    private ChiTietPhieuNhapHangRepository chiTietPhieuNhapHangRepository;
    
    @Override
    public List<ChiTietPhieuNhapHang> getAll() {
        return chiTietPhieuNhapHangRepository.findAll();
    }
    
    @Override
    public Optional<ChiTietPhieuNhapHang> findById(Integer id) {
        return chiTietPhieuNhapHangRepository.findById(id);
    }
    
    @Override
    public ChiTietPhieuNhapHang save(ChiTietPhieuNhapHang chiTietPhieuNhapHang) {
        return chiTietPhieuNhapHangRepository.save(chiTietPhieuNhapHang);
    }
    
    @Override
    public void deleteById(Integer id) {
        chiTietPhieuNhapHangRepository.deleteById(id);
    }
    
    @Override
    public List<ChiTietPhieuNhapHang> findByPhieuNhapHangId(Integer phieuNhapHangId) {
        return chiTietPhieuNhapHangRepository.findByPhieuNhapHang_Id(phieuNhapHangId);
    }
} 