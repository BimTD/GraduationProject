package org.example.graduationproject.services;

import org.example.graduationproject.models.SanPhamBienThe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SanPhamBienTheService {
    
    List<SanPhamBienThe> getAllSanPhamBienThe();
    
    Page<SanPhamBienThe> getAllPaging(Pageable pageable);
    
    SanPhamBienThe getSanPhamBienTheById(Integer id);
    
    SanPhamBienThe saveSanPhamBienThe(SanPhamBienThe sanPhamBienThe);
    
    void deleteSanPhamBienThe(Integer id);
    
    List<SanPhamBienThe> findBySanPhamId(Integer sanPhamId);
    
    List<SanPhamBienThe> searchByKeyword(String keyword);
    
    Page<SanPhamBienThe> searchByKeywordPaging(String keyword, Pageable pageable);
} 