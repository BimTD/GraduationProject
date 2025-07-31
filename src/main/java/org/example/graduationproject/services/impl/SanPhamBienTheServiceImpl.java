package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.SanPhamBienThe;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.services.SanPhamBienTheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanPhamBienTheServiceImpl implements SanPhamBienTheService {

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @Override
    public List<SanPhamBienThe> getAllSanPhamBienThe() {
        return sanPhamBienTheRepository.findAll();
    }

    @Override
    public Page<SanPhamBienThe> getAllPaging(Pageable pageable) {
        return sanPhamBienTheRepository.findAll(pageable);
    }

    @Override
    public SanPhamBienThe getSanPhamBienTheById(Integer id) {
        return sanPhamBienTheRepository.findById(id).orElse(null);
    }

    @Override
    public SanPhamBienThe saveSanPhamBienThe(SanPhamBienThe sanPhamBienThe) {
        return sanPhamBienTheRepository.save(sanPhamBienThe);
    }

    @Override
    public void deleteSanPhamBienThe(Integer id) {
        sanPhamBienTheRepository.deleteById(id);
    }

    @Override
    public List<SanPhamBienThe> findBySanPhamId(Integer sanPhamId) {
        return sanPhamBienTheRepository.findBySanPhamId(sanPhamId);
    }

    @Override
    public List<SanPhamBienThe> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSanPhamBienThe();
        }
        return sanPhamBienTheRepository.searchByKeyword(keyword);
    }

    @Override
    public Page<SanPhamBienThe> searchByKeywordPaging(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPaging(pageable);
        }
        return sanPhamBienTheRepository.searchByKeywordPaging(keyword, pageable);
    }
} 