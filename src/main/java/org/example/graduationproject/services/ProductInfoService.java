package org.example.graduationproject.services;

import org.example.graduationproject.models.Loai;
import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductInfoService {
    
    @Autowired
    private LoaiRepository loaiRepository;
    
    @Autowired
    private NhanHieuRepository nhanHieuRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    public String getProductCategoriesInfo() {
        List<Loai> categories = loaiRepository.findAll();
        if (categories.isEmpty()) {
            return "Hiện tại chưa có danh mục sản phẩm nào.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Cửa hàng có các danh mục sản phẩm sau:\n");
        
        for (Loai category : categories) {
            info.append("- ").append(category.getTen()).append("\n");
        }
        
        return info.toString();
    }
    
    public String getBrandsInfo() {
        List<NhanHieu> brands = nhanHieuRepository.findAll();
        if (brands.isEmpty()) {
            return "Hiện tại chưa có thương hiệu nào.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Cửa hàng có các thương hiệu sau:\n");
        
        for (NhanHieu brand : brands) {
            info.append("- ").append(brand.getTen()).append("\n");
        }
        
        return info.toString();
    }
    
    public String getProductsByCategory(String categoryName) {
        List<SanPham> products = sanPhamRepository.findByLoaiTenAndTrangThaiHoatDong(categoryName, true);
        if (products.isEmpty()) {
            return "Không tìm thấy sản phẩm nào trong danh mục '" + categoryName + "'.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Danh mục '").append(categoryName).append("' có các sản phẩm:\n");
        
        for (SanPham product : products) {
            info.append("- ").append(product.getTen());
            if (product.getGiaBan() != null) {
                info.append(" - ").append(product.getGiaBan()).append(" VNĐ");
            }
            info.append("\n");
        }
        
        return info.toString();
    }
    
    public String getProductSummary() {
        long totalProducts = sanPhamRepository.countByTrangThaiHoatDong(true);
        List<Loai> categories = loaiRepository.findAll();
        List<NhanHieu> brands = nhanHieuRepository.findAll();
        
        StringBuilder info = new StringBuilder();
        info.append("Tổng quan về sản phẩm:\n");
        info.append("- Tổng số sản phẩm: ").append(totalProducts).append("\n");
        info.append("- Số danh mục: ").append(categories.size()).append("\n");
        info.append("- Số thương hiệu: ").append(brands.size()).append("\n");
        
        if (!categories.isEmpty()) {
            info.append("\nCác danh mục chính:\n");
            for (Loai category : categories) {
                info.append("- ").append(category.getTen()).append("\n");
            }
        }
        
        return info.toString();
    }
    
    public String getAllProductsByCategory() {
        List<Loai> categories = loaiRepository.findAll();
        if (categories.isEmpty()) {
            return "Hiện tại chưa có danh mục sản phẩm nào.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("DANH SÁCH TẤT CẢ SẢN PHẨM THEO DANH MỤC:\n\n");
        
        for (Loai category : categories) {
            List<SanPham> products = sanPhamRepository.findByLoaiTenAndTrangThaiHoatDong(category.getTen(), true);
            
            info.append("📁 DANH MỤC: ").append(category.getTen().toUpperCase()).append("\n");
            info.append("Số sản phẩm: ").append(products.size()).append("\n");
            info.append("────────────────────────────────────\n");
            
            if (products.isEmpty()) {
                info.append("Chưa có sản phẩm nào trong danh mục này.\n\n");
                continue;
            }
            
            for (SanPham product : products) {
                info.append("🛍️ ").append(product.getTen()).append("\n");
                
                if (product.getGiaBan() != null) {
                    info.append("   💰 Giá: ").append(product.getGiaBan()).append(" VNĐ");
                    if (product.getKhuyenMai() != null && product.getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                        info.append(" (Giảm ").append(product.getKhuyenMai()).append(" VNĐ)");
                    }
                    info.append("\n");
                }
                
                if (product.getNhanHieu() != null) {
                    info.append("   🏷️ Thương hiệu: ").append(product.getNhanHieu().getTen()).append("\n");
                }
                
                if (product.getMoTa() != null && !product.getMoTa().trim().isEmpty()) {
                    String shortDesc = product.getMoTa().length() > 100 ? 
                        product.getMoTa().substring(0, 100) + "..." : product.getMoTa();
                    info.append("   📝 Mô tả: ").append(shortDesc).append("\n");
                }
                
                info.append("   🔗 Link: /product-details/").append(product.getId()).append("\n");
                info.append("\n");
            }
            info.append("\n");
        }
        
        return info.toString();
    }
    
    public String getProductDetails(String productName) {
        List<SanPham> products = sanPhamRepository.findByTenContainingIgnoreCaseAndTrangThaiHoatDong(productName, true);
        
        if (products.isEmpty()) {
            return "Không tìm thấy sản phẩm nào có tên chứa '" + productName + "'.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("KẾT QUẢ TÌM KIẾM SẢN PHẨM:\n\n");
        
        for (SanPham product : products) {
            info.append("🛍️ ").append(product.getTen()).append("\n");
            info.append("────────────────────────────────────\n");
            
            if (product.getGiaBan() != null) {
                info.append("💰 Giá bán: ").append(product.getGiaBan()).append(" VNĐ\n");
            }
            
            if (product.getGiaNhap() != null) {
                info.append("💰 Giá nhập: ").append(product.getGiaNhap()).append(" VNĐ\n");
            }
            
            if (product.getKhuyenMai() != null && product.getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                info.append("🎉 Khuyến mại: ").append(product.getKhuyenMai()).append(" VNĐ\n");
            }
            
            if (product.getLoai() != null) {
                info.append("📁 Danh mục: ").append(product.getLoai().getTen()).append("\n");
            }
            
            if (product.getNhanHieu() != null) {
                info.append("🏷️ Thương hiệu: ").append(product.getNhanHieu().getTen()).append("\n");
            }
            
            if (product.getGioiTinh() != null) {
                String gender = product.getGioiTinh() == 1 ? "Nam" : 
                               product.getGioiTinh() == 2 ? "Nữ" : "Unisex";
                info.append("👤 Giới tính: ").append(gender).append("\n");
            }
            
            if (product.getMoTa() != null && !product.getMoTa().trim().isEmpty()) {
                info.append("📝 Mô tả: ").append(product.getMoTa()).append("\n");
            }
            
            if (product.getThanhPhan() != null && !product.getThanhPhan().trim().isEmpty()) {
                info.append("🧵 Thành phần: ").append(product.getThanhPhan()).append("\n");
            }
            
            if (product.getHuongDan() != null && !product.getHuongDan().trim().isEmpty()) {
                info.append("📋 Hướng dẫn: ").append(product.getHuongDan()).append("\n");
            }
            
            if (product.getTag() != null && !product.getTag().trim().isEmpty()) {
                info.append("🏷️ Tags: ").append(product.getTag()).append("\n");
            }
            
            info.append("🔗 Xem chi tiết: /product-details/").append(product.getId()).append("\n");
            info.append("\n");
        }
        
        return info.toString();
    }
}
