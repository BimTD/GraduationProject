package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.ProductVariantDTO;
import org.example.graduationproject.models.MauSac;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.SanPhamBienThe;
import org.example.graduationproject.models.Size;
import org.example.graduationproject.repositories.MauSacRepository;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.repositories.SizeRepository;
import org.example.graduationproject.services.SanPhamBienTheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamBienTheServiceImpl implements SanPhamBienTheService {

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private MauSacRepository mauSacRepository;
    
    @Autowired
    private SizeRepository sizeRepository;

    @Override
    public List<SanPhamBienThe> getAllSanPhamBienThe() {
        return sanPhamBienTheRepository.findAll();
    }

    @Override
    public Page<SanPhamBienThe> getAllPaging(Pageable pageable) {
        return sanPhamBienTheRepository.findAll(pageable);
    }

    @Override
    public Page<SanPhamBienThe> getAllPagingWithDetails(Pageable pageable) {
        return sanPhamBienTheRepository.findAllWithDetails(pageable);
    }

    @Override
    public Optional<SanPhamBienThe> getSanPhamBienTheById(Integer id) {
        return sanPhamBienTheRepository.findById(id);
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

    @Override
    public Page<SanPhamBienThe> searchByKeywordPagingWithDetails(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPagingWithDetails(pageable);
        }
        return sanPhamBienTheRepository.searchByKeywordWithDetails(keyword, pageable);
    }
    
    @Override
    public SanPhamBienThe createProductVariant(ProductVariantDTO productVariantDTO) {
        SanPham sanPham = sanPhamRepository.findById(productVariantDTO.getSanPhamId()).orElse(null);
        MauSac mauSac = mauSacRepository.findById(productVariantDTO.getMauSacId()).orElse(null);
        Size size = sizeRepository.findById(productVariantDTO.getSizeId()).orElse(null);

        if (sanPham == null || mauSac == null || size == null) {
            throw new IllegalArgumentException("Invalid product information, color or size!");
        }

        SanPhamBienThe sanPhamBienThe = new SanPhamBienThe();
        sanPhamBienThe.setSoLuongTon(productVariantDTO.getSoLuongTon());
        sanPhamBienThe.setSanPham(sanPham);
        sanPhamBienThe.setMauSac(mauSac);
        sanPhamBienThe.setSize(size);

        return sanPhamBienTheRepository.save(sanPhamBienThe);
    }
    
    @Override
    public SanPhamBienThe updateProductVariant(ProductVariantDTO productVariantDTO) {
        SanPhamBienThe sanPhamBienThe = getSanPhamBienTheById(productVariantDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("No variant products found!"));

        SanPham sanPham = sanPhamRepository.findById(productVariantDTO.getSanPhamId()).orElse(null);
        MauSac mauSac = mauSacRepository.findById(productVariantDTO.getMauSacId()).orElse(null);
        Size size = sizeRepository.findById(productVariantDTO.getSizeId()).orElse(null);

        if (sanPham == null || mauSac == null || size == null) {
            throw new IllegalArgumentException("Invalid product information, color or size!");
        }

        sanPhamBienThe.setSoLuongTon(productVariantDTO.getSoLuongTon());
        sanPhamBienThe.setSanPham(sanPham);
        sanPhamBienThe.setMauSac(mauSac);
        sanPhamBienThe.setSize(size);

        return sanPhamBienTheRepository.save(sanPhamBienThe);
    }
    
    @Override
    public Optional<ProductVariantDTO> getProductVariantDTOById(Integer id) {
        return getSanPhamBienTheById(id)
                .map(sanPhamBienThe -> {
                    ProductVariantDTO productVariantDTO = new ProductVariantDTO();
                    productVariantDTO.setId(sanPhamBienThe.getId());
                    productVariantDTO.setSoLuongTon(sanPhamBienThe.getSoLuongTon());
                    productVariantDTO.setSanPhamId(sanPhamBienThe.getSanPham().getId());
                    productVariantDTO.setSanPhamTen(sanPhamBienThe.getSanPham().getTen());
                    productVariantDTO.setMauSacId(sanPhamBienThe.getMauSac().getId());
                    productVariantDTO.setMauSacTen(sanPhamBienThe.getMauSac().getMaMau());
                    productVariantDTO.setSizeId(sanPhamBienThe.getSize().getId());
                    productVariantDTO.setSizeTen(sanPhamBienThe.getSize().getTenSize());
                    return productVariantDTO;
                });
    }
    
    @Override
    public void deleteProductVariantById(Integer id) {
        SanPhamBienThe sanPhamBienThe = getSanPhamBienTheById(id)
                .orElseThrow(() -> new IllegalArgumentException("No variant products found!"));
        deleteSanPhamBienThe(id);
    }
} 