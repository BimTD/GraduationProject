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
    
    public String getProductsByPriceRange(String priceQuery) {
        // Trích xuất giá từ câu hỏi
        PriceRange priceRange = extractPriceRange(priceQuery);
        
        if (priceRange == null) {
            return "Tôi không hiểu mức giá bạn muốn tìm. Vui lòng cho biết cụ thể hơn, ví dụ: 'sản phẩm dưới 500000', 'sản phẩm từ 200000 đến 500000'";
        }
        
        List<SanPham> products;
        
        if (priceRange.getMinPrice() != null && priceRange.getMaxPrice() != null) {
            // Tìm sản phẩm trong khoảng giá
            products = sanPhamRepository.findByGiaBanBetweenAndTrangThaiHoatDong(
                priceRange.getMinPrice(), priceRange.getMaxPrice(), true);
        } else if (priceRange.getMinPrice() != null) {
            // Tìm sản phẩm từ giá tối thiểu trở lên
            products = sanPhamRepository.findByGiaBanGreaterThanEqualAndTrangThaiHoatDong(
                priceRange.getMinPrice(), true);
        } else if (priceRange.getMaxPrice() != null) {
            // Tìm sản phẩm dưới giá tối đa
            products = sanPhamRepository.findByGiaBanLessThanEqualAndTrangThaiHoatDong(
                priceRange.getMaxPrice(), true);
        } else {
            return "Không thể xác định mức giá từ câu hỏi của bạn.";
        }
        
        if (products.isEmpty()) {
            String rangeText = formatPriceRange(priceRange);
            return "Không tìm thấy sản phẩm nào trong khoảng giá " + rangeText + ".";
        }
        
        StringBuilder info = new StringBuilder();
        String rangeText = formatPriceRange(priceRange);
        info.append("KẾT QUẢ TÌM KIẾM SẢN PHẨM THEO GIÁ ").append(rangeText).append(":\n\n");
        info.append("Tìm thấy ").append(products.size()).append(" sản phẩm phù hợp:\n\n");
        
        for (SanPham product : products) {
            info.append("🛍️ ").append(product.getTen()).append("\n");
            info.append("────────────────────────────────────\n");
            
            if (product.getGiaBan() != null) {
                info.append("💰 Giá: ").append(product.getGiaBan()).append(" VNĐ");
                if (product.getKhuyenMai() != null && product.getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal finalPrice = product.getGiaBan().subtract(product.getKhuyenMai());
                    info.append(" (Giảm ").append(product.getKhuyenMai()).append(" VNĐ, còn ").append(finalPrice).append(" VNĐ)");
                }
                info.append("\n");
            }
            
            if (product.getLoai() != null) {
                info.append("📁 Danh mục: ").append(product.getLoai().getTen()).append("\n");
            }
            
            if (product.getNhanHieu() != null) {
                info.append("🏷️ Thương hiệu: ").append(product.getNhanHieu().getTen()).append("\n");
            }
            
            if (product.getMoTa() != null && !product.getMoTa().trim().isEmpty()) {
                String shortDesc = product.getMoTa().length() > 80 ? 
                    product.getMoTa().substring(0, 80) + "..." : product.getMoTa();
                info.append("📝 Mô tả: ").append(shortDesc).append("\n");
            }
            
            info.append("🔗 Xem chi tiết: /product-details/").append(product.getId()).append("\n");
            info.append("\n");
        }
        
        return info.toString();
    }
    
    private PriceRange extractPriceRange(String query) {
        String lowerQuery = query.toLowerCase().trim();
        
        // Tìm các số trong câu (giữ nguyên dấu - và ký tự k, m)
        String[] words = lowerQuery.split("\\s+");
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        
        // Tìm từ khóa "dưới" hoặc "dưới"
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("dưới")) {
                if (i + 1 < words.length) {
                    maxPrice = parsePrice(words[i + 1]);
                }
                break;
            }
        }
        
        // Tìm từ khóa "từ" hoặc "trên"
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("từ") || words[i].contains("trên")) {
                if (i + 1 < words.length) {
                    minPrice = parsePrice(words[i + 1]);
                }
                break;
            }
        }
        
        // Tìm từ khóa "đến" hoặc "tới"
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("đến") || words[i].contains("tới")) {
                if (i + 1 < words.length) {
                    maxPrice = parsePrice(words[i + 1]);
                }
                break;
            }
        }
        
        // Tìm khoảng giá dạng "200000-500000" hoặc "200k-500k"
        for (String word : words) {
            if (word.contains("-")) {
                String[] range = word.split("-");
                if (range.length == 2) {
                    minPrice = parsePrice(range[0]);
                    maxPrice = parsePrice(range[1]);
                    break;
                }
            }
        }
        
        // Nếu không tìm thấy từ khóa, thử tìm 2 số liên tiếp
        if (minPrice == null && maxPrice == null) {
            for (int i = 0; i < words.length - 1; i++) {
                if (isNumber(words[i]) && isNumber(words[i + 1])) {
                    minPrice = parsePrice(words[i]);
                    maxPrice = parsePrice(words[i + 1]);
                    break;
                }
            }
        }
        
        // Nếu chỉ có một số, coi như giá tối đa
        if (minPrice == null && maxPrice == null) {
            for (String word : words) {
                if (isNumber(word)) {
                    maxPrice = parsePrice(word);
                    break;
                }
            }
        }
        
        if (minPrice == null && maxPrice == null) {
            return null;
        }
        
        return new PriceRange(minPrice, maxPrice);
    }
    
    private boolean isNumber(String str) {
        return str.matches("\\d+[km]?");
    }
    
    private BigDecimal parsePrice(String priceStr) {
        try {
            if (priceStr == null || priceStr.trim().isEmpty()) {
                return null;
            }
            
            String lowerStr = priceStr.toLowerCase().trim();
            
            // Xử lý các đơn vị như k (nghìn), m (triệu)
            if (lowerStr.contains("k")) {
                String cleanPrice = lowerStr.replaceAll("[^0-9]", "");
                if (cleanPrice.isEmpty()) return null;
                return new BigDecimal(cleanPrice).multiply(new BigDecimal("1000"));
            } else if (lowerStr.contains("m")) {
                String cleanPrice = lowerStr.replaceAll("[^0-9]", "");
                if (cleanPrice.isEmpty()) return null;
                return new BigDecimal(cleanPrice).multiply(new BigDecimal("1000000"));
            } else {
                // Loại bỏ các ký tự không phải số
                String cleanPrice = priceStr.replaceAll("[^0-9]", "");
                if (cleanPrice.isEmpty()) return null;
                return new BigDecimal(cleanPrice);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String formatPriceRange(PriceRange range) {
        if (range.getMinPrice() != null && range.getMaxPrice() != null) {
            return "từ " + formatPrice(range.getMinPrice()) + " đến " + formatPrice(range.getMaxPrice());
        } else if (range.getMinPrice() != null) {
            return "từ " + formatPrice(range.getMinPrice()) + " trở lên";
        } else if (range.getMaxPrice() != null) {
            return "dưới " + formatPrice(range.getMaxPrice());
        }
        return "";
    }
    
    private String formatPrice(BigDecimal price) {
        return String.format("%,d VNĐ", price.intValue());
    }
    
    // Inner class để lưu trữ khoảng giá
    private static class PriceRange {
        private final BigDecimal minPrice;
        private final BigDecimal maxPrice;
        
        public PriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
        
        public BigDecimal getMinPrice() {
            return minPrice;
        }
        
        public BigDecimal getMaxPrice() {
            return maxPrice;
        }
    }
}
