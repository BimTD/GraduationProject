package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.ShopResponseDTO;
import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.MauSacRepository;
import org.example.graduationproject.services.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private LoaiRepository loaiRepository;
    
    @Autowired
    private NhanHieuRepository nhanHieuRepository;
    
    @Autowired
    private MauSacRepository mauSacRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ShopResponseDTO getShopPageDataWithValidation(String search, Integer categoryId, String gender, 
                                                         Integer colorId, String tag, Integer brandId, 
                                                         BigDecimal minPrice, BigDecimal maxPrice, String sort, int page, int size) {
        try {
            // Lấy tất cả categories, brands, colors
            List<Loai> categories = loaiRepository.findAll();
            List<NhanHieu> brands = nhanHieuRepository.findAll();
            List<MauSac> colors = getDistinctColorsFromProducts();
            System.out.println("Found " + colors.size() + " distinct colors: " + colors.stream().map(MauSac::getMaMau).collect(java.util.stream.Collectors.toList()));
            
            // Lấy popular tags từ database
            List<String> popularTags = getPopularTags();
            
            // Lấy sản phẩm với filters
            Page<SanPham> products = getProductsWithFilters(search, categoryId, gender, colorId, tag, brandId, minPrice, maxPrice, sort, page, size);
            
            return new ShopResponseDTO(true, "Lấy dữ liệu shop thành công", 
                                     products, categories, brands, colors, popularTags);

        } catch (Exception e) {
            e.printStackTrace();
            return new ShopResponseDTO(false, "Có lỗi xảy ra: " + e.getMessage());
        }
    }
    
    private Page<SanPham> getProductsWithFilters(String search, Integer categoryId, String gender, 
                                                 Integer colorId, String tag, Integer brandId, 
                                                 BigDecimal minPrice, BigDecimal maxPrice, String sort, int page, int size) {
        // Sử dụng JPA Criteria API để tạo dynamic query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SanPham> query = cb.createQuery(SanPham.class);
        Root<SanPham> root = query.from(SanPham.class);
        
        // Tạo danh sách các điều kiện (predicates)
        List<Predicate> predicates = createPredicates(cb, root, search, categoryId, gender, colorId, tag, brandId, minPrice, maxPrice);
        
        // Kết hợp tất cả điều kiện bằng AND
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        // Sắp xếp theo yêu cầu
        applySorting(query, cb, root, sort);
        
        // Thực hiện truy vấn để đếm tổng số bản ghi
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SanPham> countRoot = countQuery.from(SanPham.class);
        
        // Sử dụng helper method để tạo predicates cho count query
        List<Predicate> countPredicates = createPredicates(cb, countRoot, search, categoryId, gender, colorId, tag, brandId, minPrice, maxPrice);
        
        if (!countPredicates.isEmpty()) {
            countQuery.where(countPredicates.toArray(new Predicate[0]));
        }
        countQuery.select(cb.count(countRoot));
        
        // Thực hiện count query
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        
        // Thực hiện query chính với phân trang
        TypedQuery<SanPham> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);
        
        List<SanPham> content = typedQuery.getResultList();
        
        // Tạo Page object
        Pageable pageable = PageRequest.of(page, size);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, totalElements);
    }
    
    /**
     * Helper method để tạo danh sách predicates cho cả query chính và count query
     */
    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<SanPham> root, 
                                           String search, Integer categoryId, String gender, 
                                           Integer colorId, String tag, Integer brandId,
                                           BigDecimal minPrice, BigDecimal maxPrice) {
        List<Predicate> predicates = new ArrayList<>();
        
        // 1. Điều kiện tìm kiếm - CHỈ TÌM THEO TÊN SẢN PHẨM
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = "%" + search.trim().toLowerCase() + "%";
            // Chỉ tìm kiếm trong trường tên sản phẩm
            predicates.add(cb.like(cb.lower(root.get("ten")), searchTerm));
        }
        
        // 2. Điều kiện lọc theo danh mục (category)
        if (categoryId != null) {
            Join<SanPham, Loai> loaiJoin = root.join("loai");
            predicates.add(cb.equal(loaiJoin.get("id"), categoryId));
        }
        
        // 2.1. Điều kiện lọc theo thương hiệu (brand)
        if (brandId != null) {
            System.out.println("Adding brandId filter: " + brandId);
            Join<SanPham, NhanHieu> nhanHieuJoin = root.join("nhanHieu");
            predicates.add(cb.equal(nhanHieuJoin.get("id"), brandId));
        }
        
        // 3. Điều kiện lọc theo giới tính
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                Integer genderValue = Integer.parseInt(gender);
                predicates.add(cb.equal(root.get("gioiTinh"), genderValue));
            } catch (NumberFormatException e) {
                // Bỏ qua nếu gender không phải số
            }
        }
        
        
        // 5. Điều kiện lọc theo màu sắc (theo tên màu, không phải ID)
        if (colorId != null) {
            System.out.println("Adding colorId filter: " + colorId);
            
            // Lấy tên màu từ colorId
            String colorName = getColorNameById(colorId);
            if (colorName != null) {
                System.out.println("Filtering by color name: " + colorName);
                Join<SanPham, SanPhamBienThe> bienTheJoin = root.join("bienThes");
                Join<SanPhamBienThe, MauSac> mauSacJoin = bienTheJoin.join("mauSac");
                // Filter theo tên màu thay vì ID
                predicates.add(cb.equal(mauSacJoin.get("maMau"), colorName));
            }
        }
        
        // 6. Điều kiện lọc theo tag
        if (tag != null && !tag.trim().isEmpty()) {
            System.out.println("Adding tag filter: " + tag);
            predicates.add(cb.like(cb.lower(root.get("tag")), "%" + tag.trim().toLowerCase() + "%"));
        }
        
        // 7. Điều kiện lọc theo giá
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Adding minPrice filter: " + minPrice);
            predicates.add(cb.greaterThanOrEqualTo(root.get("giaBan"), minPrice));
        }
        
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Adding maxPrice filter: " + maxPrice);
            predicates.add(cb.lessThanOrEqualTo(root.get("giaBan"), maxPrice));
        }
        
        // 8. Điều kiện chỉ hiển thị sản phẩm đang hoạt động
        predicates.add(cb.equal(root.get("trangThaiHoatDong"), true));
        
        return predicates;
    }
    
    
    /**
     * Lấy tên màu từ ID
     */
    private String getColorNameById(Integer colorId) {
        try {
            MauSac color = mauSacRepository.findById(colorId).orElse(null);
            return color != null ? color.getMaMau() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy danh sách màu sắc duy nhất từ sản phẩm (1 màu cho mỗi loại)
     */
    private List<MauSac> getDistinctColorsFromProducts() {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<MauSac> query = cb.createQuery(MauSac.class);
            Root<SanPham> root = query.from(SanPham.class);
            
            // Join với SanPhamBienThe và MauSac
            Join<SanPham, SanPhamBienThe> bienTheJoin = root.join("bienThes");
            Join<SanPhamBienThe, MauSac> mauSacJoin = bienTheJoin.join("mauSac");
            
            // Select distinct MauSac theo loại (maMau)
            query.select(mauSacJoin).distinct(true);
            
            // Chỉ lấy sản phẩm đang hoạt động
            query.where(cb.equal(root.get("trangThaiHoatDong"), true));
            
            // Sắp xếp theo tên màu
            query.orderBy(cb.asc(mauSacJoin.get("maMau")));
            
            List<MauSac> allColors = entityManager.createQuery(query).getResultList();
            
            // Lọc để chỉ lấy 1 màu cho mỗi loại (maMau)
            Map<String, MauSac> uniqueColorsByType = new LinkedHashMap<>();
            for (MauSac color : allColors) {
                String colorType = color.getMaMau(); // Lấy loại màu (ví dụ: "Đỏ", "Xanh", "Vàng")
                if (!uniqueColorsByType.containsKey(colorType)) {
                    uniqueColorsByType.put(colorType, color);
                }
            }
            
            List<MauSac> result = new ArrayList<>(uniqueColorsByType.values());
            System.out.println("Filtered to " + result.size() + " unique color types: " + result.stream().map(MauSac::getMaMau).collect(java.util.stream.Collectors.toList()));
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: lấy tất cả màu từ repository
            return mauSacRepository.findAll();
        }
    }
    
    /**
     * Lấy danh sách popular tags từ database
     */
    private List<String> getPopularTags() {
        try {
            // Query để lấy tất cả tags không null và không rỗng
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<String> query = cb.createQuery(String.class);
            Root<SanPham> root = query.from(SanPham.class);
            
            query.select(root.get("tag"))
                 .where(cb.and(
                     cb.isNotNull(root.get("tag")),
                     cb.notEqual(root.get("tag"), ""),
                     cb.equal(root.get("trangThaiHoatDong"), true)
                 ))
                 .distinct(true);
            
            List<String> allTags = entityManager.createQuery(query).getResultList();
            
            // Lọc và xử lý tags
            return allTags.stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .flatMap(tag -> java.util.Arrays.stream(tag.split(",")))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .distinct()
                    .limit(10) // Chỉ lấy 10 tags phổ biến nhất
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback tags nếu có lỗi
            return java.util.Arrays.asList("new", "sale", "hot", "trending", "popular");
        }
    }
    
    /**
     * Áp dụng sorting cho query
     */
    private void applySorting(CriteriaQuery<SanPham> query, CriteriaBuilder cb, Root<SanPham> root, String sort) {
        if (sort != null && !sort.trim().isEmpty()) {
            switch (sort) {
                case "price_asc":
                    query.orderBy(cb.asc(root.get("giaBan")));
                    break;
                case "price_desc":
                    query.orderBy(cb.desc(root.get("giaBan")));
                    break;
                case "name_asc":
                    query.orderBy(cb.asc(root.get("ten")));
                    break;
                case "name_desc":
                    query.orderBy(cb.desc(root.get("ten")));
                    break;
                case "newest":
                    query.orderBy(cb.desc(root.get("ngayTao")));
                    break;
                case "oldest":
                    query.orderBy(cb.asc(root.get("ngayTao")));
                    break;
                default:
                    // Default sorting by name ascending
                    query.orderBy(cb.asc(root.get("ten")));
                    break;
            }
        } else {
            // Default sorting by name ascending
            query.orderBy(cb.asc(root.get("ten")));
        }
    }
}
