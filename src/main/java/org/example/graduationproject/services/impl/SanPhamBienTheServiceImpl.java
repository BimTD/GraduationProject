package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.BulkProductVariantDTO;
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

import java.util.ArrayList;
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
    
    @Override
    public List<SanPhamBienThe> createBulkProductVariants(BulkProductVariantDTO bulkDTO) {
        SanPham sanPham = sanPhamRepository.findById(bulkDTO.getSanPhamId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
        
        if (bulkDTO.getMauSacIds() == null || bulkDTO.getMauSacIds().isEmpty()) {
            throw new IllegalArgumentException("Color list cannot be empty!");
        }
        
        if (bulkDTO.getSizeIds() == null || bulkDTO.getSizeIds().isEmpty()) {
            throw new IllegalArgumentException("Size list cannot be empty!");
        }
        
        List<SanPhamBienThe> createdVariants = new ArrayList<>();
        
        for (Integer mauSacId : bulkDTO.getMauSacIds()) {
            for (Integer sizeId : bulkDTO.getSizeIds()) {
                // Kiểm tra xem biến thể đã tồn tại chưa
                if (!existsProductVariant(bulkDTO.getSanPhamId(), mauSacId, sizeId)) {
                    MauSac mauSac = mauSacRepository.findById(mauSacId)
                            .orElseThrow(() -> new IllegalArgumentException("Color not found with ID: " + mauSacId));
                    Size size = sizeRepository.findById(sizeId)
                            .orElseThrow(() -> new IllegalArgumentException("Size not found with ID: " + sizeId));
                    
                    SanPhamBienThe sanPhamBienThe = new SanPhamBienThe();
                    sanPhamBienThe.setSoLuongTon(bulkDTO.getSoLuongTon());
                    sanPhamBienThe.setSanPham(sanPham);
                    sanPhamBienThe.setMauSac(mauSac);
                    sanPhamBienThe.setSize(size);
                    
                    SanPhamBienThe savedVariant = sanPhamBienTheRepository.save(sanPhamBienThe);
                    createdVariants.add(savedVariant);
                }
            }
        }
        
        return createdVariants;
    }
    
    @Override
    public BulkProductVariantDTO.BulkCreateResult createBulkProductVariantsWithResult(BulkProductVariantDTO bulkDTO) {
        SanPham sanPham = sanPhamRepository.findById(bulkDTO.getSanPhamId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
        
        if (bulkDTO.getMauSacIds() == null || bulkDTO.getMauSacIds().isEmpty()) {
            throw new IllegalArgumentException("Color list cannot be empty!");
        }
        
        if (bulkDTO.getSizeIds() == null || bulkDTO.getSizeIds().isEmpty()) {
            throw new IllegalArgumentException("Size list cannot be empty!");
        }
        
        List<SanPhamBienThe> createdVariants = new ArrayList<>();
        List<String> createdVariantDetails = new ArrayList<>();
        List<String> existingVariantDetails = new ArrayList<>();
        
        int totalRequested = bulkDTO.getMauSacIds().size() * bulkDTO.getSizeIds().size();
        int createdNew = 0;
        int alreadyExists = 0;
        
        for (Integer mauSacId : bulkDTO.getMauSacIds()) {
            MauSac mauSac = mauSacRepository.findById(mauSacId)
                    .orElseThrow(() -> new IllegalArgumentException("Color not found with ID: " + mauSacId));
            
            for (Integer sizeId : bulkDTO.getSizeIds()) {
                Size size = sizeRepository.findById(sizeId)
                        .orElseThrow(() -> new IllegalArgumentException("Size not found with ID: " + sizeId));
                
                String variantDetail = String.format("%s - %s (%s)", 
                    sanPham.getTen(), 
                    mauSac.getMaMau(), 
                    size.getTenSize());
                
                // Kiểm tra xem biến thể đã tồn tại chưa
                if (!existsProductVariant(bulkDTO.getSanPhamId(), mauSacId, sizeId)) {
                    SanPhamBienThe sanPhamBienThe = new SanPhamBienThe();
                    sanPhamBienThe.setSoLuongTon(bulkDTO.getSoLuongTon());
                    sanPhamBienThe.setSanPham(sanPham);
                    sanPhamBienThe.setMauSac(mauSac);
                    sanPhamBienThe.setSize(size);
                    
                    SanPhamBienThe savedVariant = sanPhamBienTheRepository.save(sanPhamBienThe);
                    createdVariants.add(savedVariant);
                    createdVariantDetails.add(variantDetail);
                    createdNew++;
                } else {
                    existingVariantDetails.add(variantDetail);
                    alreadyExists++;
                }
            }
        }
        
        // Tạo thông báo tổng hợp
        String message;
        if (createdNew > 0 && alreadyExists > 0) {
            message = String.format("Đã tạo %d biến thể mới, %d biến thể đã tồn tại", createdNew, alreadyExists);
        } else if (createdNew > 0) {
            message = String.format("Đã tạo thành công %d biến thể mới", createdNew);
        } else {
            message = String.format("Tất cả %d biến thể đã tồn tại", alreadyExists);
        }
        
        return new BulkProductVariantDTO.BulkCreateResult(
            totalRequested, createdNew, alreadyExists, 
            createdVariantDetails, existingVariantDetails, message
        );
    }
    
    @Override
    public boolean existsProductVariant(Integer sanPhamId, Integer mauSacId, Integer sizeId) {
        return sanPhamBienTheRepository.existsBySanPhamIdAndMauSacIdAndSizeId(sanPhamId, mauSacId, sizeId);
    }
    
    @Override
    public BulkProductVariantDTO getAvailableColorsAndSizesForProduct(Integer sanPhamId) {
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
        
        BulkProductVariantDTO bulkDTO = new BulkProductVariantDTO();
        bulkDTO.setSanPhamId(sanPhamId);
        bulkDTO.setSanPhamTen(sanPham.getTen());
        
        // Lấy loại sản phẩm để xác định màu sắc và size phù hợp
        if (sanPham.getLoai() != null) {
            Integer loaiId = sanPham.getLoai().getId();
            
            // Lấy màu sắc phù hợp với loại sản phẩm
            List<MauSac> availableColors = mauSacRepository.findByLoaiId(loaiId);
            List<BulkProductVariantDTO.MauSacInfo> colorInfos = availableColors.stream()
                    .map(color -> new BulkProductVariantDTO.MauSacInfo(
                            color.getId(),
                            color.getMaMau(),
                            color.getLoai() != null ? color.getLoai().getTen() : null
                    ))
                    .toList();
            bulkDTO.setAvailableColors(colorInfos);
            
            // Lấy size phù hợp với loại sản phẩm
            List<Size> availableSizes = sizeRepository.findByLoaiId(loaiId);
            List<BulkProductVariantDTO.SizeInfo> sizeInfos = availableSizes.stream()
                    .map(size -> new BulkProductVariantDTO.SizeInfo(
                            size.getId(),
                            size.getTenSize(),
                            size.getLoai() != null ? size.getLoai().getTen() : null
                    ))
                    .toList();
            bulkDTO.setAvailableSizes(sizeInfos);
        } else {
            // Nếu không có loại, lấy tất cả màu sắc và size
            List<MauSac> allColors = mauSacRepository.findAll();
            List<BulkProductVariantDTO.MauSacInfo> colorInfos = allColors.stream()
                    .map(color -> new BulkProductVariantDTO.MauSacInfo(
                            color.getId(),
                            color.getMaMau(),
                            color.getLoai() != null ? color.getLoai().getTen() : null
                    ))
                    .toList();
            bulkDTO.setAvailableColors(colorInfos);
            
            List<Size> allSizes = sizeRepository.findAll();
            List<BulkProductVariantDTO.SizeInfo> sizeInfos = allSizes.stream()
                    .map(size -> new BulkProductVariantDTO.SizeInfo(
                            size.getId(),
                            size.getTenSize(),
                            size.getLoai() != null ? size.getLoai().getTen() : null
                    ))
                    .toList();
            bulkDTO.setAvailableSizes(sizeInfos);
        }
        
        return bulkDTO;
    }
} 