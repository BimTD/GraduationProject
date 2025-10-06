package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.*;
import org.example.graduationproject.dto.ProductDTO;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.NhaCungCapRepository;
import org.example.graduationproject.repositories.ImageSanPhamRepository;
import org.example.graduationproject.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private ImageSanPhamRepository imageSanPhamRepository;
    @Autowired
    private LoaiRepository loaiRepository;
    @Autowired
    private NhanHieuRepository nhanHieuRepository;
    @Autowired
    private NhaCungCapRepository nhaCungCapRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SanPham> getAll() {
        return this.sanPhamRepository.findAll();
    }

    @Override
    public List<SanPham> getByGioiTinh(Integer gioiTinh) {
        return this.sanPhamRepository.findByGioiTinh(gioiTinh);
    }

    @Override
    public Boolean create(SanPham sanPham) {
        try{
            this.sanPhamRepository.save(sanPham);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<SanPham> findById(Integer id) {
        return this.sanPhamRepository.findById(id);
    }

    @Override
    public Boolean deleteById(Integer id) {
        try {
            // Xóa toàn bộ ảnh liên quan trước
            imageSanPhamRepository.deleteAll(imageSanPhamRepository.findAllBySanPham_Id(id));
            this.sanPhamRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean update(SanPham sanPham) {
        try {
            this.sanPhamRepository.save(sanPham);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SanPham save(SanPham sanPham) {
        return this.sanPhamRepository.save(sanPham);
    }

    @Override
    public void updateActiveStatus(Integer id, boolean active) {
        SanPham sp = sanPhamRepository.findById(id).orElse(null);
        if (sp != null) {
            sp.setTrangThaiHoatDong(active);
            sanPhamRepository.save(sp);
        }
    }

    @Override
    public void saveProductWithUrls(ProductDTO productDTO, String imageUrls) throws Exception {
        // Kiểm tra tên sản phẩm có trùng không
        if (!isProductNameAvailable(productDTO.getTen(), null)) {
            throw new Exception("Tên sản phẩm '" + productDTO.getTen() + "' đã tồn tại. Vui lòng chọn tên khác.");
        }
        
        // Kiểm tra giá bán phải lớn hơn giá nhập
        if (productDTO.getGiaBan() != null && productDTO.getGiaNhap() != null) {
            if (productDTO.getGiaBan().compareTo(productDTO.getGiaNhap()) <= 0) {
                throw new Exception("Giá bán phải lớn hơn giá nhập.");
            }
        }
        
        SanPham sanPham = new SanPham();
        sanPham.setTen(productDTO.getTen());
        sanPham.setMoTa(productDTO.getMoTa());
        sanPham.setGiaBan(productDTO.getGiaBan());
        sanPham.setGiaNhap(productDTO.getGiaNhap());
        sanPham.setKhuyenMai(productDTO.getKhuyenMai());
        sanPham.setTag(productDTO.getTag());
        sanPham.setHuongDan(productDTO.getHuongDan());
        sanPham.setThanhPhan(productDTO.getThanhPhan());
        sanPham.setNgayTao(java.time.LocalDateTime.now());
        sanPham.setNgayCapNhat(java.time.LocalDateTime.now());
        sanPham.setTrangThaiSanPham("new");
        sanPham.setTrangThaiHoatDong(true);
        sanPham.setGioiTinh(productDTO.getGioiTinh());
        sanPham.setLoai(loaiRepository.findById(productDTO.getLoaiId()).orElse(null));
        sanPham.setNhanHieu(nhanHieuRepository.findById(productDTO.getNhanHieuId()).orElse(null));
        sanPham.setNhaCungCap(nhaCungCapRepository.findById(productDTO.getNhaCungCapId()).orElse(null));
        sanPham = sanPhamRepository.save(sanPham);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String[] urls = imageUrls.split(",");
            for (String url : urls) {
                ImageSanPham image = new ImageSanPham();
                image.setImageName(url.trim());
                image.setSanPham(sanPham);
                imageSanPhamRepository.save(image);
            }
        }
    }

    @Override
    public void updateProductWithUrls(ProductDTO productDTO, String imageUrls) throws Exception {
        SanPham sanPham = sanPhamRepository.findById(productDTO.getId()).orElse(null);
        if (sanPham == null) {
            throw new Exception("Không tìm thấy sản phẩm có id: " + productDTO.getId());
        }
        
        // Kiểm tra tên sản phẩm có trùng không (loại trừ sản phẩm hiện tại)
        if (!isProductNameAvailable(productDTO.getTen(), productDTO.getId())) {
            throw new Exception("Tên sản phẩm '" + productDTO.getTen() + "' đã tồn tại. Vui lòng chọn tên khác.");
        }
        
        // Kiểm tra giá bán phải lớn hơn giá nhập
        if (productDTO.getGiaBan() != null && productDTO.getGiaNhap() != null) {
            if (productDTO.getGiaBan().compareTo(productDTO.getGiaNhap()) <= 0) {
                throw new Exception("Giá bán phải lớn hơn giá nhập.");
            }
        }
        
        sanPham.setTen(productDTO.getTen());
        sanPham.setMoTa(productDTO.getMoTa());
        sanPham.setGiaBan(productDTO.getGiaBan());
        sanPham.setGiaNhap(productDTO.getGiaNhap());
        sanPham.setKhuyenMai(productDTO.getKhuyenMai());
        sanPham.setTag(productDTO.getTag());
        sanPham.setHuongDan(productDTO.getHuongDan());
        sanPham.setThanhPhan(productDTO.getThanhPhan());
        sanPham.setNgayCapNhat(java.time.LocalDateTime.now());
        sanPham.setGioiTinh(productDTO.getGioiTinh());
        sanPham.setLoai(loaiRepository.findById(productDTO.getLoaiId()).orElse(null));
        sanPham.setNhanHieu(nhanHieuRepository.findById(productDTO.getNhanHieuId()).orElse(null));
        sanPham.setNhaCungCap(nhaCungCapRepository.findById(productDTO.getNhaCungCapId()).orElse(null));
        
        sanPham = sanPhamRepository.save(sanPham);
        
        // Chỉ xử lý ảnh nếu có imageUrls được cung cấp
        if (imageUrls != null && !imageUrls.trim().isEmpty()) {
            // Xóa tất cả ảnh cũ
            imageSanPhamRepository.deleteAll(imageSanPhamRepository.findAllBySanPham_Id(sanPham.getId()));
            
            // Thêm ảnh mới
            String[] urls = imageUrls.split(",");
            for (String url : urls) {
                if (!url.trim().isEmpty()) {
                    ImageSanPham image = new ImageSanPham();
                    image.setImageName(url.trim());
                    image.setSanPham(sanPham);
                    imageSanPhamRepository.save(image);
                }
            }
        }
    }
    
    // Phân trang
    @Override
    public Page<SanPham> getAllPaging(int page, int size) {
        return sanPhamRepository.findAll(PageRequest.of(page, size));
    }
    
    @Override
    public Page<SanPham> searchByTenPaging(String ten, int page, int size) {
        return sanPhamRepository.findByTenContainingIgnoreCase(ten, PageRequest.of(page, size));
    }
    
    // Filter methods
    @Override
    public Page<SanPham> filterByCategoryPaging(Integer categoryId, int page, int size) {
        return sanPhamRepository.findByLoai_Id(categoryId, PageRequest.of(page, size));
    }
    
    @Override
    public Page<SanPham> filterByGenderPaging(Integer gender, int page, int size) {
        return sanPhamRepository.findByGioiTinh(gender, PageRequest.of(page, size));
    }
    
    @Override
    public Page<SanPham> filterByCategoryAndGenderPaging(Integer categoryId, Integer gender, int page, int size) {
        return sanPhamRepository.findByLoai_IdAndGioiTinh(categoryId, gender, PageRequest.of(page, size));
    }
    
    // Search with filters
    @Override
    public Page<SanPham> searchByTenAndCategoryPaging(String ten, Integer categoryId, int page, int size) {
        return sanPhamRepository.findByTenContainingIgnoreCaseAndLoai_Id(ten, categoryId, PageRequest.of(page, size));
    }
    
    @Override
    public Page<SanPham> searchByTenAndGenderPaging(String ten, Integer gender, int page, int size) {
        return sanPhamRepository.findByTenContainingIgnoreCaseAndGioiTinh(ten, gender, PageRequest.of(page, size));
    }
    
    @Override
    public Page<SanPham> searchByTenAndCategoryAndGenderPaging(String ten, Integer categoryId, Integer gender, int page, int size) {
        return sanPhamRepository.findByTenContainingIgnoreCaseAndLoai_IdAndGioiTinh(ten, categoryId, gender, PageRequest.of(page, size));
    }
    
    @Override
    public Page<SanPham> getProductsWithFilters(String search, Integer categoryId, String gender, int page, int size) {
        // Sử dụng JPA Criteria API để tạo dynamic query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SanPham> query = cb.createQuery(SanPham.class);
        Root<SanPham> root = query.from(SanPham.class);
        
        // Tạo danh sách các điều kiện (predicates) sử dụng helper method
        List<Predicate> predicates = createPredicates(cb, root, search, categoryId, gender);
        
        // Kết hợp tất cả điều kiện bằng AND
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        // Sắp xếp theo tên sản phẩm
        query.orderBy(cb.asc(root.get("ten")));
        
        // Thực hiện truy vấn để đếm tổng số bản ghi
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SanPham> countRoot = countQuery.from(SanPham.class);
        
        // Sử dụng helper method để tạo predicates cho count query
        List<Predicate> countPredicates = createPredicates(cb, countRoot, search, categoryId, gender);
        
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
        return new PageImpl<>(content, pageable, totalElements);
    }
    
    /**
     * Method dành cho admin - hiển thị tất cả sản phẩm (cả active và inactive)
     */
    public Page<SanPham> getProductsWithFiltersForAdmin(String search, Integer categoryId, String gender, int page, int size) {
        // Sử dụng JPA Criteria API để tạo dynamic query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SanPham> query = cb.createQuery(SanPham.class);
        Root<SanPham> root = query.from(SanPham.class);
        
        // Tạo danh sách các điều kiện (predicates) sử dụng helper method cho admin
        List<Predicate> predicates = createPredicatesForAdmin(cb, root, search, categoryId, gender);
        
        // Kết hợp tất cả điều kiện bằng AND
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        // Sắp xếp theo tên sản phẩm
        query.orderBy(cb.asc(root.get("ten")));
        
        // Thực hiện truy vấn để đếm tổng số bản ghi
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SanPham> countRoot = countQuery.from(SanPham.class);
        
        // Sử dụng helper method để tạo predicates cho count query
        List<Predicate> countPredicates = createPredicatesForAdmin(cb, countRoot, search, categoryId, gender);
        
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
        return new PageImpl<>(content, pageable, totalElements);
    }
    
    /**
     * Helper method để tạo danh sách predicates cho cả query chính và count query
     */
    private List<Predicate> createPredicates(CriteriaBuilder cb, Root<SanPham> root, 
                                           String search, Integer categoryId, String gender) {
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
        
        // 3. Điều kiện lọc theo giới tính
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                Integer genderValue = Integer.parseInt(gender);
                predicates.add(cb.equal(root.get("gioiTinh"), genderValue));
            } catch (NumberFormatException e) {
                // Bỏ qua nếu gender không phải số
            }
        }
        
        // 4. Điều kiện chỉ hiển thị sản phẩm đang hoạt động
        predicates.add(cb.equal(root.get("trangThaiHoatDong"), true));
        
        return predicates;
    }
    
    /**
     * Helper method để tạo danh sách predicates cho admin - không filter theo trạng thái hoạt động
     */
    private List<Predicate> createPredicatesForAdmin(CriteriaBuilder cb, Root<SanPham> root, 
                                                    String search, Integer categoryId, String gender) {
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
        
        // 3. Điều kiện lọc theo giới tính
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                Integer genderValue = Integer.parseInt(gender);
                predicates.add(cb.equal(root.get("gioiTinh"), genderValue));
            } catch (NumberFormatException e) {
                // Bỏ qua nếu gender không phải số
            }
        }
        
        // 4. KHÔNG filter theo trạng thái hoạt động - admin cần thấy tất cả sản phẩm
        
        return predicates;
    }

    
    @Override
    public Optional<ProductDTO> getProductDTOById(Integer id) {
        return findById(id)
                .map(sanPham -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(sanPham.getId());
                    productDTO.setTen(sanPham.getTen());
                    productDTO.setMoTa(sanPham.getMoTa());
                    productDTO.setGiaBan(sanPham.getGiaBan());
                    productDTO.setGiaNhap(sanPham.getGiaNhap());
                    productDTO.setKhuyenMai(sanPham.getKhuyenMai());
                    productDTO.setTag(sanPham.getTag());
                    productDTO.setHuongDan(sanPham.getHuongDan());
                    productDTO.setThanhPhan(sanPham.getThanhPhan());
                    productDTO.setGioiTinh(sanPham.getGioiTinh());
                    productDTO.setLoaiId(sanPham.getLoai() != null ? sanPham.getLoai().getId() : null);
                    productDTO.setNhanHieuId(sanPham.getNhanHieu() != null ? sanPham.getNhanHieu().getId() : null);
                    productDTO.setNhaCungCapId(sanPham.getNhaCungCap() != null ? sanPham.getNhaCungCap().getId() : null);
                    return productDTO;
                });
    }
    
    @Override
    public void deleteProductById(Integer id) {
        SanPham sanPham = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm nào!"));
        deleteById(id);
    }
    
    @Override
    public void deleteImageById(Integer imageId) {
        imageSanPhamRepository.deleteById(imageId);
    }
    
    @Override
    public void toggleProductActiveStatus(Integer id, boolean active) {
        SanPham sanPham = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm nào!"));
        sanPham.setTrangThaiHoatDong(active);
        sanPhamRepository.save(sanPham);
    }
    
    @Override
    public boolean isProductNameAvailable(String name, Integer excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Nếu có excludeId (chế độ edit), kiểm tra tên trùng với các sản phẩm khác
        if (excludeId != null) {
            return !sanPhamRepository.existsByTenIgnoreCaseAndIdNot(name.trim(), excludeId);
        } else {
            // Nếu không có excludeId (chế độ thêm mới), kiểm tra tên trùng với tất cả sản phẩm
            return !sanPhamRepository.existsByTenIgnoreCase(name.trim());
        }
    }
    
    @Override
    public List<SanPham> getProductsBySupplierId(Integer supplierId) {
        return sanPhamRepository.findByNhaCungCapId(supplierId);
    }
    
    @Override
    public List<SanPham> getNewestProducts(int limit) {
        if (limit <= 0) {
            return sanPhamRepository.findTop6ByTrangThaiHoatDongTrueOrderByNgayTaoDesc();
        }
        Pageable pageable = PageRequest.of(0, limit);
        return sanPhamRepository.findByTrangThaiHoatDongTrueOrderByNgayTaoDesc(pageable).getContent();
    }
    
    @Override
    public Page<SanPham> getNewestProductsPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sanPhamRepository.findByTrangThaiHoatDongTrueOrderByNgayTaoDesc(pageable);
    }
}
