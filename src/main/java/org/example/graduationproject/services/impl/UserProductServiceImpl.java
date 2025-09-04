package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.ProductResponseDTO;
import org.example.graduationproject.exceptions.ResourceNotFoundException;
import org.example.graduationproject.exceptions.ValidationException;
import org.example.graduationproject.models.ImageSanPham;
import org.example.graduationproject.models.MauSac;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.SanPhamBienThe;
import org.example.graduationproject.models.Size;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.services.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProductServiceImpl implements UserProductService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @Override
    public ProductResponseDTO getProductQuickViewWithValidation(Integer productId) {
        // Validate input
        if (productId == null || productId <= 0) {
            throw new ValidationException("ID sản phẩm không hợp lệ");
        }

        Optional<SanPham> productOpt = sanPhamRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }

        SanPham product = productOpt.get();
        List<SanPhamBienThe> variants = sanPhamBienTheRepository.findBySanPhamId(productId);

        String image = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ImageSanPham first = product.getImages().get(0);
            image = first != null ? first.getImageName() : null;
        }

        // Distinct sizes
        List<Map<String, Object>> sizes = variants.stream()
                .map(SanPhamBienThe::getSize)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Size::getId,
                                s -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", s.getId());
                                    m.put("name", s.getTenSize());
                                    return m;
                                },
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        // Distinct colors
        List<Map<String, Object>> colors = variants.stream()
                .map(SanPhamBienThe::getMauSac)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                MauSac::getId,
                                c -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", c.getId());
                                    m.put("name", c.getMaMau());
                                    return m;
                                },
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        // Variants list
        List<Map<String, Object>> variantList = new ArrayList<>();
        for (SanPhamBienThe v : variants) {
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", v.getId());
            vm.put("sizeId", v.getSize() != null ? v.getSize().getId() : null);
            vm.put("colorId", v.getMauSac() != null ? v.getMauSac().getId() : null);
            vm.put("stock", v.getSoLuongTon());
            variantList.add(vm);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", product.getId());
        body.put("name", product.getTen());
        body.put("price", product.getGiaBan() != null ? product.getGiaBan() : BigDecimal.ZERO);
        body.put("discount", product.getKhuyenMai());
        body.put("image", image);
        body.put("description", product.getMoTa());
        body.put("sizes", sizes);
        body.put("colors", colors);
        body.put("variants", variantList);

        return new ProductResponseDTO(true, "Lấy thông tin sản phẩm thành công", body);
    }

    @Override
    public ProductResponseDTO getProductDetailsWithValidation(Integer productId) {
        // Validate input
        if (productId == null || productId <= 0) {
            throw new ValidationException("ID sản phẩm không hợp lệ");
        }

        Optional<SanPham> productOpt = sanPhamRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }

        SanPham product = productOpt.get();
        
        // Check if product is active
        if (product.getTrangThaiHoatDong() == null || !product.getTrangThaiHoatDong()) {
            throw new ResourceNotFoundException("Sản phẩm không còn hoạt động");
        }

        List<SanPhamBienThe> variants = sanPhamBienTheRepository.findBySanPhamId(productId);

        // Get all images
        List<Map<String, Object>> images = new ArrayList<>();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ImageSanPham img : product.getImages()) {
                Map<String, Object> imgMap = new LinkedHashMap<>();
                imgMap.put("id", img.getId());
                imgMap.put("imageName", img.getImageName());
                images.add(imgMap);
            }
        }

        // Distinct sizes
        List<Map<String, Object>> sizes = variants.stream()
                .map(SanPhamBienThe::getSize)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Size::getId,
                                s -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", s.getId());
                                    m.put("name", s.getTenSize());
                                    return m;
                                },
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        // Distinct colors
        List<Map<String, Object>> colors = variants.stream()
                .map(SanPhamBienThe::getMauSac)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                MauSac::getId,
                                c -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", c.getId());
                                    m.put("name", c.getMaMau());
                                    return m;
                                },
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        // Variants list
        List<Map<String, Object>> variantList = new ArrayList<>();
        for (SanPhamBienThe v : variants) {
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", v.getId());
            vm.put("sizeId", v.getSize() != null ? v.getSize().getId() : null);
            vm.put("colorId", v.getMauSac() != null ? v.getMauSac().getId() : null);
            vm.put("stock", v.getSoLuongTon());
            variantList.add(vm);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", product.getId());
        body.put("name", product.getTen());
        body.put("price", product.getGiaBan() != null ? product.getGiaBan() : BigDecimal.ZERO);
        body.put("discount", product.getKhuyenMai());
        body.put("description", product.getMoTa());
        body.put("tag", product.getTag());
        body.put("huongDan", product.getHuongDan());
        body.put("thanhPhan", product.getThanhPhan());
        body.put("images", images);
        body.put("sizes", sizes);
        body.put("colors", colors);
        body.put("variants", variantList);
        body.put("category", product.getLoai() != null ? product.getLoai().getTen() : null);
        body.put("brand", product.getNhanHieu() != null ? product.getNhanHieu().getTen() : null);

        return new ProductResponseDTO(true, "Lấy thông tin chi tiết sản phẩm thành công", body);
    }

    @Override
    public ProductResponseDTO getRelatedProductsWithValidation(Integer productId, int limit) {
        // Validate input
        if (productId == null || productId <= 0) {
            throw new ValidationException("ID sản phẩm không hợp lệ");
        }

        if (limit <= 0) {
            limit = 8; // Default limit
        }

        // Get current product
        Optional<SanPham> productOpt = sanPhamRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }

        SanPham currentProduct = productOpt.get();
        
        // Get related products (same category and gender, excluding current product)
        List<SanPham> relatedProducts = sanPhamRepository.findByLoai_IdAndGioiTinhAndIdNotAndTrangThaiHoatDongTrue(
            currentProduct.getLoai().getId(), 
            currentProduct.getGioiTinh(), 
            productId,
            PageRequest.of(0, limit)
        ).getContent();

        // Convert to response format
        List<Map<String, Object>> productsList = new ArrayList<>();
        for (SanPham product : relatedProducts) {
            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", product.getId());
            productMap.put("name", product.getTen());
            productMap.put("price", product.getGiaBan() != null ? product.getGiaBan() : BigDecimal.ZERO);
            productMap.put("discount", product.getKhuyenMai());
            
            // Get first image
            String image = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                ImageSanPham first = product.getImages().get(0);
                image = first != null ? first.getImageName() : null;
            }
            productMap.put("image", image);
            
            productsList.add(productMap);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("products", productsList);
        body.put("total", productsList.size());

        return new ProductResponseDTO(true, "Lấy sản phẩm liên quan thành công", body);
    }

    @Override
    public ProductResponseDTO getUpsellProductsWithValidation(Integer productId, int limit) {
        // Validate input
        if (productId == null || productId <= 0) {
            throw new ValidationException("ID sản phẩm không hợp lệ");
        }

        if (limit <= 0) {
            limit = 8; // Default limit
        }

        // Get current product
        Optional<SanPham> productOpt = sanPhamRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }

        SanPham currentProduct = productOpt.get();
        List<SanPham> upsellProducts = new ArrayList<>();

        // Strategy 1: Same brand with discount (preferred)
        if (currentProduct.getNhanHieu() != null) {
            List<SanPham> brandProducts = sanPhamRepository.findByNhanHieu_IdAndIdNotAndTrangThaiHoatDongTrueAndKhuyenMaiGreaterThanOrderByKhuyenMaiDesc(
                currentProduct.getNhanHieu().getId(), 
                productId, 
                BigDecimal.ZERO, // Any discount > 0
                PageRequest.of(0, limit)
            ).getContent();
            upsellProducts.addAll(brandProducts);
        }

        // Strategy 2: Higher price products in same category (if not enough from brand)
        if (upsellProducts.size() < limit && currentProduct.getLoai() != null) {
            int remainingLimit = limit - upsellProducts.size();
            List<SanPham> higherPriceProducts = sanPhamRepository.findByLoai_IdAndGioiTinhAndIdNotAndTrangThaiHoatDongTrueAndGiaBanGreaterThanOrderByGiaBanDesc(
                currentProduct.getLoai().getId(),
                currentProduct.getGioiTinh(),
                productId,
                currentProduct.getGiaBan() != null ? currentProduct.getGiaBan() : BigDecimal.ZERO,
                PageRequest.of(0, remainingLimit)
            ).getContent();
            
            // Filter out products already in upsell list
            List<Integer> existingIds = upsellProducts.stream().map(SanPham::getId).toList();
            List<SanPham> filteredProducts = higherPriceProducts.stream()
                .filter(p -> !existingIds.contains(p.getId()))
                .toList();
            
            upsellProducts.addAll(filteredProducts);
        }

        // Convert to response format
        List<Map<String, Object>> productsList = new ArrayList<>();
        for (SanPham product : upsellProducts) {
            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", product.getId());
            productMap.put("name", product.getTen());
            productMap.put("price", product.getGiaBan() != null ? product.getGiaBan() : BigDecimal.ZERO);
            productMap.put("discount", product.getKhuyenMai());
            
            // Get first image
            String image = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                ImageSanPham first = product.getImages().get(0);
                image = first != null ? first.getImageName() : null;
            }
            productMap.put("image", image);
            
            productsList.add(productMap);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("products", productsList);

        return new ProductResponseDTO(true, "Lấy sản phẩm upsell thành công", body);
    }
}
