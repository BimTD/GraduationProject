package org.example.graduationproject.services.impl;

import jakarta.persistence.TypedQuery;
import org.example.graduationproject.dto.SearchResponseDTO;
import org.example.graduationproject.dto.SearchSuggestionDTO;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Cache cho popular searches (có thể thay bằng Redis trong production)
    private static final List<String> POPULAR_SEARCHES = Arrays.asList(
        "áo thun", "quần jean", "váy", "áo sơ mi", "giày sneaker", 
        "túi xách", "áo khoác", "quần short", "đầm", "áo len"
    );

    // Cache cho search history (có thể lưu vào database)
    private static final Map<String, List<String>> SEARCH_HISTORY = new HashMap<>();

    @Override
    public SearchResponseDTO getSearchSuggestions(String query, int limit) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return new SearchResponseDTO(true, "Nhập từ khóa để tìm kiếm", new ArrayList<>());
            }

            String trimmedQuery = query.trim().toLowerCase();
            String searchTerm = "%" + trimmedQuery + "%";
            String exactTerm = trimmedQuery; // Tìm kiếm chính xác
            String startTerm = trimmedQuery + "%"; // Tìm kiếm bắt đầu với từ khóa
            
            // Tìm kiếm sản phẩm với fuzzy search
            List<SanPham> products = sanPhamRepository.findTopSuggestions(searchTerm, exactTerm, startTerm, PageRequest.of(0, limit));
            
            // Chuyển đổi sang DTO để tránh circular reference
            List<SearchSuggestionDTO> suggestions = products.stream()
                .map(this::convertToSuggestionDTO)
                .collect(Collectors.toList());
            
            return new SearchResponseDTO(true, "Tìm thấy " + suggestions.size() + " gợi ý", new ArrayList<>(suggestions));
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResponseDTO(false, "Lỗi tìm kiếm gợi ý: " + e.getMessage());
        }
    }

    @Override
    public SearchResponseDTO quickSearch(String query, Integer categoryId, int limit) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return new SearchResponseDTO(false, "Vui lòng nhập từ khóa tìm kiếm");
            }

            String trimmedQuery = query.trim().toLowerCase();
            String searchTerm = "%" + trimmedQuery + "%";
            String exactTerm = trimmedQuery; // Tìm kiếm chính xác
            String startTerm = trimmedQuery + "%"; // Tìm kiếm bắt đầu với từ khóa
            
            Pageable pageable = PageRequest.of(0, limit, Sort.by("ngayTao").descending());
            
            Page<SanPham> products;
            if (categoryId != null) {
                products = sanPhamRepository.findBySearchTermAndCategory(searchTerm, exactTerm, startTerm, categoryId, pageable);
            } else {
                products = sanPhamRepository.findBySearchTerm(searchTerm, exactTerm, startTerm, pageable);
            }
            
            return new SearchResponseDTO(true, "Tìm thấy " + products.getTotalElements() + " sản phẩm", products);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResponseDTO(false, "Lỗi tìm kiếm nhanh: " + e.getMessage());
        }
    }

    @Override
    public SearchResponseDTO getPopularSearches(int limit) {
        try {
            List<String> popular = POPULAR_SEARCHES.stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            return new SearchResponseDTO(true, "Danh sách tìm kiếm phổ biến", popular, true);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResponseDTO(false, "Lỗi lấy tìm kiếm phổ biến: " + e.getMessage());
        }
    }

    @Override
    public SearchResponseDTO advancedSearch(String query, Integer categoryId, Integer brandId, 
                                          Integer colorId, String gender, BigDecimal minPrice, 
                                          BigDecimal maxPrice, String tag, String sort, 
                                          int page, int size) {
        try {
            // Tạo dynamic query với Criteria API
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<SanPham> cq = cb.createQuery(SanPham.class);
            Root<SanPham> root = cq.from(SanPham.class);
            
            List<Predicate> predicates = new ArrayList<>();
            
            // Tìm kiếm theo từ khóa với fuzzy search
            if (query != null && !query.trim().isEmpty()) {
                String trimmedQuery = query.trim().toLowerCase();
                String searchTerm = "%" + trimmedQuery + "%";
                
                // Tìm kiếm trong nhiều trường
                Join<SanPham, Object> loaiJoin = root.join("loai", JoinType.LEFT);
                Join<SanPham, Object> nhanHieuJoin = root.join("nhanHieu", JoinType.LEFT);
                
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("ten")), searchTerm),
                    cb.like(cb.lower(root.get("moTa")), searchTerm),
                    cb.like(cb.lower(root.get("tag")), searchTerm),
                    cb.like(cb.lower(loaiJoin.get("ten")), searchTerm),
                    cb.like(cb.lower(nhanHieuJoin.get("ten")), searchTerm)
                ));
            }
            
            // Lọc theo danh mục
            if (categoryId != null) {
                Join<SanPham, Object> categoryJoin = root.join("loai");
                predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            }
            
            // Lọc theo thương hiệu
            if (brandId != null) {
                Join<SanPham, Object> brandJoin = root.join("nhanHieu");
                predicates.add(cb.equal(brandJoin.get("id"), brandId));
            }
            
            // Lọc theo màu sắc
            if (colorId != null) {
                Join<SanPham, Object> colorJoin = root.join("bienThes");
                Join<Object, Object> colorVariantJoin = colorJoin.join("mauSac");
                predicates.add(cb.equal(colorVariantJoin.get("id"), colorId));
            }
            
            // Lọc theo giới tính
            if (gender != null && !gender.trim().isEmpty()) {
                try {
                    Integer genderValue = Integer.parseInt(gender);
                    predicates.add(cb.equal(root.get("gioiTinh"), genderValue));
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu gender không phải số
                }
            }
            
            // Lọc theo giá
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("giaBan"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("giaBan"), maxPrice));
            }
            
            // Lọc theo tag
            if (tag != null && !tag.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("tag")), "%" + tag.toLowerCase() + "%"));
            }
            
            // Chỉ lấy sản phẩm đang hoạt động
            predicates.add(cb.equal(root.get("trangThaiHoatDong"), true));
            
            // Kết hợp tất cả điều kiện
            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }
            
            // Sắp xếp
            applySorting(cq, cb, root, sort);
            
            // Thực hiện query
            TypedQuery<SanPham> typedQuery = entityManager.createQuery(cq);
            
            // Phân trang
            Pageable pageable = PageRequest.of(page, size);
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
            
            List<SanPham> results = typedQuery.getResultList();
            
            // Đếm tổng số bản ghi
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<SanPham> countRoot = countQuery.from(SanPham.class);
            
            List<Predicate> countPredicates = new ArrayList<>();
            if (query != null && !query.trim().isEmpty()) {
                String searchTerm = "%" + query.trim().toLowerCase() + "%";
                countPredicates.add(cb.or(
                    cb.like(cb.lower(countRoot.get("ten")), searchTerm),
                    cb.like(cb.lower(countRoot.get("moTa")), searchTerm),
                    cb.like(cb.lower(countRoot.get("tag")), searchTerm)
                ));
            }
            if (categoryId != null) {
                Join<SanPham, Object> categoryJoin = countRoot.join("loai");
                countPredicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            }
            if (brandId != null) {
                Join<SanPham, Object> brandJoin = countRoot.join("nhanHieu");
                countPredicates.add(cb.equal(brandJoin.get("id"), brandId));
            }
            if (colorId != null) {
                Join<SanPham, Object> colorJoin = countRoot.join("bienThes");
                Join<Object, Object> colorVariantJoin = colorJoin.join("mauSac");
                countPredicates.add(cb.equal(colorVariantJoin.get("id"), colorId));
            }
            if (gender != null && !gender.trim().isEmpty()) {
                try {
                    Integer genderValue = Integer.parseInt(gender);
                    countPredicates.add(cb.equal(countRoot.get("gioiTinh"), genderValue));
                } catch (NumberFormatException e) {
                    // Bỏ qua
                }
            }
            if (minPrice != null) {
                countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("giaBan"), minPrice));
            }
            if (maxPrice != null) {
                countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("giaBan"), maxPrice));
            }
            if (tag != null && !tag.trim().isEmpty()) {
                countPredicates.add(cb.like(cb.lower(countRoot.get("tag")), "%" + tag.toLowerCase() + "%"));
            }
            countPredicates.add(cb.equal(countRoot.get("trangThaiHoatDong"), true));
            
            if (!countPredicates.isEmpty()) {
                countQuery.where(countPredicates.toArray(new Predicate[0]));
            }
            countQuery.select(cb.count(countRoot));
            
            Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
            
            // Tạo Page object
            Page<SanPham> productPage = new org.springframework.data.domain.PageImpl<>(
                results, pageable, totalElements);
            
            return new SearchResponseDTO(true, "Tìm thấy " + totalElements + " sản phẩm", productPage);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResponseDTO(false, "Lỗi tìm kiếm nâng cao: " + e.getMessage());
        }
    }

    @Override
    public SearchResponseDTO saveSearchHistory(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return new SearchResponseDTO(false, "Query không được để trống");
            }
            
            // Lưu vào cache (trong production nên lưu vào database)
            String userId = "anonymous"; // Có thể lấy từ SecurityContext
            SEARCH_HISTORY.computeIfAbsent(userId, k -> new ArrayList<>()).add(query.trim());
            
            return new SearchResponseDTO(true, "Đã lưu lịch sử tìm kiếm");
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResponseDTO(false, "Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    @Override
    public SearchResponseDTO getSearchHistory(int limit) {
        try {
            String userId = "anonymous"; // Có thể lấy từ SecurityContext
            List<String> history = SEARCH_HISTORY.getOrDefault(userId, new ArrayList<>());
            
            // Lấy limit gần nhất
            List<String> recentHistory = history.stream()
                .skip(Math.max(0, history.size() - limit))
                .collect(Collectors.toList());
            
            return new SearchResponseDTO(true, "Lịch sử tìm kiếm", recentHistory, 1);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResponseDTO(false, "Lỗi lấy lịch sử: " + e.getMessage());
        }
    }

    private void applySorting(CriteriaQuery<SanPham> cq, CriteriaBuilder cb, Root<SanPham> root, String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            cq.orderBy(cb.desc(root.get("ngayTao")));
            return;
        }

        switch (sort) {
            case "name_asc":
                cq.orderBy(cb.asc(root.get("ten")));
                break;
            case "name_desc":
                cq.orderBy(cb.desc(root.get("ten")));
                break;
            case "price_asc":
                cq.orderBy(cb.asc(root.get("giaBan")));
                break;
            case "price_desc":
                cq.orderBy(cb.desc(root.get("giaBan")));
                break;
            case "newest":
                cq.orderBy(cb.desc(root.get("ngayTao")));
                break;
            case "oldest":
                cq.orderBy(cb.asc(root.get("ngayTao")));
                break;
            default:
                cq.orderBy(cb.desc(root.get("ngayTao")));
                break;
        }
    }

    private SearchSuggestionDTO convertToSuggestionDTO(SanPham product) {
        SearchSuggestionDTO dto = new SearchSuggestionDTO();
        dto.setId(product.getId());
        dto.setTen(product.getTen());
        dto.setMoTa(product.getMoTa());
        
        // Format price
        if (product.getGiaBan() != null) {
            dto.setGiaBan(formatPrice(product.getGiaBan()));
        } else {
            dto.setGiaBan("0 VNĐ");
        }
        
        // Get first image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setImageUrl(product.getImages().get(0).getImageName());
        } else {
            dto.setImageUrl("/fe/img/product/product15.jpg");
        }
        
        // Get category name
        if (product.getLoai() != null) {
            dto.setCategoryName(product.getLoai().getTen());
        } else {
            dto.setCategoryName("");
        }
        
        // Get brand name
        if (product.getNhanHieu() != null) {
            dto.setBrandName(product.getNhanHieu().getTen());
        } else {
            dto.setBrandName("");
        }
        
        return dto;
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 VNĐ";
        return new java.text.DecimalFormat("#,###").format(price) + " VNĐ";
    }
}
