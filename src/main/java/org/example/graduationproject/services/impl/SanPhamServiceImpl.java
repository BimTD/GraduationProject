package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.dto.ProductDTO;
import org.example.graduationproject.models.ImageSanPham;
import org.example.graduationproject.repositories.ImageSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.NhaCungCapRepository;

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
            throw new Exception("Product not found with id: " + productDTO.getId());
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
        // Xử lý logic filter và search
        if (search != null && !search.trim().isEmpty()) {
            // Có search
            if (categoryId != null && gender != null && !gender.trim().isEmpty()) {
                // Search + Category + Gender
                return searchByTenAndCategoryAndGenderPaging(search, categoryId, Integer.parseInt(gender), page, size);
            } else if (categoryId != null) {
                // Search + Category
                return searchByTenAndCategoryPaging(search, categoryId, page, size);
            } else if (gender != null && !gender.trim().isEmpty()) {
                // Search + Gender
                return searchByTenAndGenderPaging(search, Integer.parseInt(gender), page, size);
            } else {
                // Chỉ search
                return searchByTenPaging(search, page, size);
            }
        } else {
            // Không có search
            if (categoryId != null && gender != null && !gender.trim().isEmpty()) {
                // Category + Gender
                return filterByCategoryAndGenderPaging(categoryId, Integer.parseInt(gender), page, size);
            } else if (categoryId != null) {
                // Chỉ Category
                return filterByCategoryPaging(categoryId, page, size);
            } else if (gender != null && !gender.trim().isEmpty()) {
                // Chỉ Gender
                return filterByGenderPaging(Integer.parseInt(gender), page, size);
            } else {
                // Không có filter
                return getAllPaging(page, size);
            }
        }
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
                .orElseThrow(() -> new IllegalArgumentException("No products found!"));
        deleteById(id);
    }
    
    @Override
    public void deleteImageById(Integer imageId) {
        imageSanPhamRepository.deleteById(imageId);
    }
    
    @Override
    public void toggleProductActiveStatus(Integer id, boolean active) {
        SanPham sanPham = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No products found!"));
        sanPham.setTrangThaiHoatDong(active);
        sanPhamRepository.save(sanPham);
    }
}
