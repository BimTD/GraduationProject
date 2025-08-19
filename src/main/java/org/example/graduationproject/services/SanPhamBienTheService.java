package org.example.graduationproject.services;

import org.example.graduationproject.dto.BulkProductVariantDTO;
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
    
    // Phương thức mới để tạo biến thể hàng loạt
    List<SanPhamBienThe> createBulkProductVariants(BulkProductVariantDTO bulkDTO);
    
    // Phương thức mới để tạo biến thể hàng loạt và trả về kết quả chi tiết
    BulkProductVariantDTO.BulkCreateResult createBulkProductVariantsWithResult(BulkProductVariantDTO bulkDTO);
    
    // Phương thức để kiểm tra biến thể đã tồn tại
    boolean existsProductVariant(Integer sanPhamId, Integer mauSacId, Integer sizeId);
    
    // Phương thức mới để lấy màu sắc và size phù hợp với sản phẩm
    BulkProductVariantDTO getAvailableColorsAndSizesForProduct(Integer sanPhamId);
} 