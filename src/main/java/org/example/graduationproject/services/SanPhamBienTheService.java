package org.example.graduationproject.services;

import org.example.graduationproject.dto.ProductVariantDTO;
import org.example.graduationproject.models.SanPhamBienThe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SanPhamBienTheService {
    
    List<SanPhamBienThe> getAllSanPhamBienThe();
    
    Page<SanPhamBienThe> getAllPaging(Pageable pageable);
    
    Page<SanPhamBienThe> getAllPagingWithDetails(Pageable pageable);
    
    Optional<SanPhamBienThe> getSanPhamBienTheById(Integer id);
    
    SanPhamBienThe saveSanPhamBienThe(SanPhamBienThe sanPhamBienThe);
    
    void deleteSanPhamBienThe(Integer id);
    
    List<SanPhamBienThe> findBySanPhamId(Integer sanPhamId);
    
    List<SanPhamBienThe> searchByKeyword(String keyword);
    
    Page<SanPhamBienThe> searchByKeywordPaging(String keyword, Pageable pageable);
    
    Page<SanPhamBienThe> searchByKeywordPagingWithDetails(String keyword, Pageable pageable);
    
    SanPhamBienThe createProductVariant(ProductVariantDTO productVariantDTO);
    
    SanPhamBienThe updateProductVariant(ProductVariantDTO productVariantDTO);
    
    Optional<ProductVariantDTO> getProductVariantDTOById(Integer id);
    
    void deleteProductVariantById(Integer id);
} 