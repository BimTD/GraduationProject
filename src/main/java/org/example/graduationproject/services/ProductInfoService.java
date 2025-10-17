package org.example.graduationproject.services;

import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class ProductInfoService {
    
    @Autowired
    private LoaiRepository loaiRepository;
    
    @Autowired
    private NhanHieuRepository nhanHieuRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private MauSacRepository mauSacRepository;
    
    @Autowired
    private SizeRepository sizeRepository;
    
    @Autowired
    private NhaCungCapRepository nhaCungCapRepository;
    
    public String getProductCategoriesInfo() {
        List<Loai> categories = loaiRepository.findAll();
        if (categories.isEmpty()) {
            return "Hiện tại chưa có danh mục sản phẩm nào.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("📁 DANH SÁCH CÁC DANH MỤC SẢN PHẨM:\n\n");
        
        for (Loai category : categories) {
            // Đếm số sản phẩm trong mỗi danh mục
            long productCount = sanPhamRepository.countByLoaiTenAndTrangThaiHoatDong(category.getTen(), true);
            
            info.append("🏷️ ").append(category.getTen()).append("\n");
            info.append("   📊 Số sản phẩm: ").append(productCount).append("\n");
            
            // Thêm emoji phù hợp với từng danh mục
            String emoji = getCategoryEmoji(category.getTen());
            if (!emoji.isEmpty()) {
                info.append("   ").append(emoji).append(" Mô tả: ").append(getCategoryDescription(category.getTen())).append("\n");
            }
            
            info.append("\n");
        }
        
        info.append("💡 Gợi ý: Bạn có thể hỏi chi tiết về bất kỳ danh mục nào, ví dụ:\n");
        info.append("• 'Sản phẩm áo thun'\n");
        info.append("• 'Danh mục giày'\n");
        info.append("• 'Quần jean có gì?'\n");
        
        return info.toString();
    }
    
    private String getCategoryEmoji(String categoryName) {
        String lowerName = categoryName.toLowerCase();
        if (lowerName.contains("áo") || lowerName.contains("shirt")) {
            return "👕";
        } else if (lowerName.contains("quần") || lowerName.contains("pant")) {
            return "👖";
        } else if (lowerName.contains("giày") || lowerName.contains("shoe")) {
            return "👟";
        } else if (lowerName.contains("túi") || lowerName.contains("bag")) {
            return "👜";
        } else if (lowerName.contains("mũ") || lowerName.contains("hat")) {
            return "🧢";
        } else if (lowerName.contains("kính") || lowerName.contains("glass")) {
            return "🕶️";
        } else if (lowerName.contains("đồng hồ") || lowerName.contains("watch")) {
            return "⌚";
        } else if (lowerName.contains("váy") || lowerName.contains("dress")) {
            return "👗";
        } else if (lowerName.contains("phụ kiện") || lowerName.contains("accessory")) {
            return "💍";
        }
        return "📦";
    }
    
    private String getCategoryDescription(String categoryName) {
        String lowerName = categoryName.toLowerCase();
        if (lowerName.contains("áo thun")) {
            return "Áo thun thoải mái, phong cách trẻ trung";
        } else if (lowerName.contains("quần jean")) {
            return "Quần jean bền đẹp, phong cách cá tính";
        } else if (lowerName.contains("giày")) {
            return "Giày thể thao và giày thời trang";
        } else if (lowerName.contains("túi")) {
            return "Túi xách, balo, ví da cao cấp";
        } else if (lowerName.contains("mũ")) {
            return "Mũ nón thời trang, bảo vệ khỏi nắng";
        } else if (lowerName.contains("kính")) {
            return "Kính mát, kính cận thời trang";
        } else if (lowerName.contains("đồng hồ")) {
            return "Đồng hồ nam nữ, phụ kiện sang trọng";
        } else if (lowerName.contains("váy")) {
            return "Váy đầm nữ tính, thanh lịch";
        } else if (lowerName.contains("phụ kiện")) {
            return "Phụ kiện thời trang, trang sức";
        }
        return "Sản phẩm chất lượng cao";
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
            return "Không tìm thấy sản phẩm nào trong danh mục '" + categoryName + "'. Vui lòng kiểm tra lại tên danh mục hoặc liên hệ hotline: 0982172169";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("🛍️ TẤT CẢ SẢN PHẨM DANH MỤC '").append(categoryName.toUpperCase()).append("':\n\n");
        info.append("📊 Tổng số sản phẩm: ").append(products.size()).append("\n");
        info.append("═══════════════════════════════════════\n\n");
        
        for (SanPham product : products) {
            info.append("🛍️ ").append(product.getTen()).append("\n");
            info.append("────────────────────────────────────\n");
            
            if (product.getGiaBan() != null) {
                info.append("💰 Giá: ").append(formatPrice(product.getGiaBan()));
                if (product.getKhuyenMai() != null && product.getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal finalPrice = product.getGiaBan().subtract(product.getKhuyenMai());
                    info.append(" (Giảm ").append(formatPrice(product.getKhuyenMai()))
                          .append(", còn ").append(formatPrice(finalPrice)).append(")");
                }
                info.append("\n");
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
                String shortDesc = product.getMoTa().length() > 100 ? 
                    product.getMoTa().substring(0, 100) + "..." : product.getMoTa();
                info.append("📝 Mô tả: ").append(shortDesc).append("\n");
            }
            
            // Lấy thông tin biến thể (màu sắc, size)
            if (product.getBienThes() != null && !product.getBienThes().isEmpty()) {
                info.append("🎨 Màu sắc: ");
                product.getBienThes().stream()
                    .map(bienThe -> bienThe.getMauSac().getMaMau())
                    .distinct()
                    .forEach(color -> info.append(color).append(" "));
                info.append("\n");
                
                info.append("📏 Size: ");
                product.getBienThes().stream()
                    .map(bienThe -> bienThe.getSize().getTenSize())
                    .distinct()
                    .forEach(size -> info.append(size).append(" "));
                info.append("\n");
            }
            
            info.append("🔗 Xem chi tiết: /product-details/").append(product.getId()).append("\n");
            info.append("\n");
        }
        
        info.append("💡 Gợi ý: Bạn có thể hỏi thêm:\n");
        info.append("• 'Sản phẩm ").append(categoryName).append(" dưới 500k'\n");
        info.append("• 'Sản phẩm ").append(categoryName).append(" màu đỏ'\n");
        info.append("• 'Sản phẩm ").append(categoryName).append(" size M'\n");
        
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
    
    // ==================== CÁC PHƯƠNG THỨC MỚI CHO CHATBOT THÔNG MINH ====================
    
    /**
     * Tìm kiếm sản phẩm theo nhiều tiêu chí thông minh
     */
    public String searchProductsIntelligently(String query) {
        String lowerQuery = query.toLowerCase().trim();
        StringBuilder result = new StringBuilder();
        
        // 1. Tìm kiếm theo tên sản phẩm
        List<SanPham> productsByName = sanPhamRepository.findByTenContainingIgnoreCaseAndTrangThaiHoatDong(query, true);
        if (!productsByName.isEmpty()) {
            result.append("🔍 KẾT QUẢ TÌM KIẾM THEO TÊN:\n");
            result.append(formatProductList(productsByName, 5));
            result.append("\n");
        }
        
        // 2. Tìm kiếm theo thương hiệu
        String brandName = extractBrandName(query);
        if (!brandName.isEmpty()) {
            List<SanPham> productsByBrand = findProductsByBrand(brandName);
            if (!productsByBrand.isEmpty()) {
                result.append("🏷️ SẢN PHẨM THƯƠNG HIỆU ").append(brandName.toUpperCase()).append(":\n");
                result.append(formatProductList(productsByBrand, 5));
                result.append("\n");
            }
        }
        
        // 3. Tìm kiếm theo danh mục
        String categoryName = extractCategoryName(query);
        if (!categoryName.isEmpty()) {
            List<SanPham> productsByCategory = sanPhamRepository.findByLoaiTenAndTrangThaiHoatDong(categoryName, true);
            if (!productsByCategory.isEmpty()) {
                result.append("📁 SẢN PHẨM DANH MỤC ").append(categoryName.toUpperCase()).append(":\n");
                result.append(formatProductList(productsByCategory, 5));
                result.append("\n");
            }
        }
        
        // 4. Tìm kiếm theo màu sắc
        String colorName = extractColorName(query);
        if (!colorName.isEmpty()) {
            List<SanPham> productsByColor = findProductsByColor(colorName);
            if (!productsByColor.isEmpty()) {
                result.append("🎨 SẢN PHẨM MÀU ").append(colorName.toUpperCase()).append(":\n");
                result.append(formatProductList(productsByColor, 5));
                result.append("\n");
            }
        }
        
        // 5. Tìm kiếm theo size
        String sizeName = extractSizeName(query);
        if (!sizeName.isEmpty()) {
            List<SanPham> productsBySize = findProductsBySize(sizeName);
            if (!productsBySize.isEmpty()) {
                result.append("📏 SẢN PHẨM SIZE ").append(sizeName.toUpperCase()).append(":\n");
                result.append(formatProductList(productsBySize, 5));
                result.append("\n");
            }
        }
        
        // 6. Tìm kiếm theo giới tính
        Integer gender = extractGender(query);
        if (gender != null) {
            List<SanPham> productsByGender = sanPhamRepository.findByGioiTinhAndTrangThaiHoatDong(gender, true);
            if (!productsByGender.isEmpty()) {
                String genderText = gender == 1 ? "NAM" : gender == 2 ? "NỮ" : "UNISEX";
                result.append("👤 SẢN PHẨM CHO ").append(genderText).append(":\n");
                result.append(formatProductList(productsByGender, 5));
                result.append("\n");
            }
        }
        
        // 7. Tìm kiếm theo tag
        String tagName = extractTagName(query);
        if (!tagName.isEmpty()) {
            List<SanPham> productsByTag = findProductsByTag(tagName);
            if (!productsByTag.isEmpty()) {
                result.append("🏷️ SẢN PHẨM TAG ").append(tagName.toUpperCase()).append(":\n");
                result.append(formatProductList(productsByTag, 5));
                result.append("\n");
            }
        }
        
        if (result.length() == 0) {
            return "Không tìm thấy sản phẩm nào phù hợp với từ khóa '" + query + "'. Vui lòng thử từ khóa khác hoặc liên hệ hotline: 0982172169";
        }
        
        return result.toString();
    }
    
    /**
     * Lấy thông tin chi tiết về sản phẩm theo ID
     */
    public String getProductDetailsById(Integer productId) {
        return sanPhamRepository.findById(productId)
                .map(this::formatProductDetails)
                .orElse("Không tìm thấy sản phẩm với ID: " + productId);
    }
    
    /**
     * Lấy sản phẩm mới nhất
     */
    public String getNewestProducts(int limit) {
        List<SanPham> newestProducts = sanPhamRepository.findTop6ByTrangThaiHoatDongTrueOrderByNgayTaoDesc();
        if (newestProducts.isEmpty()) {
            return "Hiện tại chưa có sản phẩm mới nào.";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("🆕 SẢN PHẨM MỚI NHẤT:\n\n");
        result.append(formatProductList(newestProducts, limit));
        return result.toString();
    }
    
    /**
     * Lấy sản phẩm khuyến mại
     */
    public String getDiscountedProducts() {
        List<SanPham> discountedProducts = sanPhamRepository.findByTrangThaiHoatDongTrueAndKhuyenMaiGreaterThanOrderByKhuyenMaiDesc(BigDecimal.ZERO);
        if (discountedProducts.isEmpty()) {
            return "Hiện tại chưa có sản phẩm khuyến mại nào.";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("🎉 SẢN PHẨM KHUYẾN MẠI:\n\n");
        result.append(formatProductList(discountedProducts, 10));
        return result.toString();
    }
    
    /**
     * Lấy sản phẩm theo khoảng giá thông minh
     */
    public String getProductsByPriceRangeSmart(String priceQuery) {
        PriceRange priceRange = extractPriceRange(priceQuery);
        if (priceRange == null) {
            return "Tôi không hiểu mức giá bạn muốn tìm. Vui lòng cho biết cụ thể hơn, ví dụ: 'sản phẩm dưới 500k', 'sản phẩm từ 200k đến 500k'";
        }
        
        List<SanPham> products;
        
        if (priceRange.getMinPrice() != null && priceRange.getMaxPrice() != null) {
            products = sanPhamRepository.findByGiaBanBetweenAndTrangThaiHoatDong(
                priceRange.getMinPrice(), priceRange.getMaxPrice(), true);
        } else if (priceRange.getMinPrice() != null) {
            products = sanPhamRepository.findByGiaBanGreaterThanEqualAndTrangThaiHoatDong(
                priceRange.getMinPrice(), true);
        } else if (priceRange.getMaxPrice() != null) {
            products = sanPhamRepository.findByGiaBanLessThanEqualAndTrangThaiHoatDong(
                priceRange.getMaxPrice(), true);
        } else {
            return "Không thể xác định mức giá từ câu hỏi của bạn.";
        }
        
        if (products.isEmpty()) {
            String rangeText = formatPriceRange(priceRange);
            return "Không tìm thấy sản phẩm nào trong khoảng giá " + rangeText + ".";
        }
        
        StringBuilder result = new StringBuilder();
        String rangeText = formatPriceRange(priceRange);
        result.append("💰 SẢN PHẨM THEO GIÁ ").append(rangeText).append(":\n\n");
        result.append("Tìm thấy ").append(products.size()).append(" sản phẩm phù hợp:\n\n");
        result.append(formatProductList(products, 10));
        return result.toString();
    }
    
    /**
     * Lấy thông tin tổng quan về cửa hàng
     */
    public String getStoreOverview() {
        long totalProducts = sanPhamRepository.countByTrangThaiHoatDong(true);
        List<Loai> categories = loaiRepository.findAll();
        List<NhanHieu> brands = nhanHieuRepository.findAll();
        List<MauSac> colors = mauSacRepository.findAll();
        List<Size> sizes = sizeRepository.findAll();
        
        StringBuilder info = new StringBuilder();
        info.append("🏪 THÔNG TIN CỬA HÀNG SHOP REID:\n\n");
        info.append("📊 TỔNG QUAN:\n");
        info.append("• Tổng số sản phẩm: ").append(totalProducts).append("\n");
        info.append("• Số danh mục: ").append(categories.size()).append("\n");
        info.append("• Số thương hiệu: ").append(brands.size()).append("\n");
        info.append("• Số màu sắc: ").append(colors.size()).append("\n");
        info.append("• Số size: ").append(sizes.size()).append("\n\n");
        
        if (!categories.isEmpty()) {
            info.append("📁 DANH MỤC SẢN PHẨM:\n");
            for (Loai category : categories) {
                long count = sanPhamRepository.countByLoaiTenAndTrangThaiHoatDong(category.getTen(), true);
                info.append("• ").append(category.getTen()).append(" (").append(count).append(" sản phẩm)\n");
            }
            info.append("\n");
        }
        
        if (!brands.isEmpty()) {
            info.append("🏷️ THƯƠNG HIỆU:\n");
            for (NhanHieu brand : brands) {
                long count = sanPhamRepository.countByNhanHieuTenAndTrangThaiHoatDong(brand.getTen(), true);
                info.append("• ").append(brand.getTen()).append(" (").append(count).append(" sản phẩm)\n");
            }
            info.append("\n");
        }
        
        info.append("💡 GỢI Ý TÌM KIẾM:\n");
        info.append("• Hỏi về sản phẩm cụ thể: 'áo thun nike', 'giày adidas'\n");
        info.append("• Tìm theo giá: 'sản phẩm dưới 500k', 'từ 200k đến 500k'\n");
        info.append("• Tìm theo màu: 'áo màu đỏ', 'giày màu trắng'\n");
        info.append("• Tìm theo size: 'áo size M', 'giày size 42'\n");
        info.append("• Tìm theo giới tính: 'áo nam', 'váy nữ'\n");
        info.append("• Xem sản phẩm mới: 'sản phẩm mới nhất'\n");
        info.append("• Xem khuyến mại: 'sản phẩm khuyến mại'\n");
        
        return info.toString();
    }
    
    // ==================== CÁC PHƯƠNG THỨC HỖ TRỢ ====================
    
    private String formatProductList(List<SanPham> products, int limit) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        
        for (SanPham product : products) {
            if (count >= limit) break;
            
            result.append("🛍️ ").append(product.getTen()).append("\n");
            result.append("────────────────────────────────────\n");
            
            if (product.getGiaBan() != null) {
                result.append("💰 Giá: ").append(formatPrice(product.getGiaBan()));
                if (product.getKhuyenMai() != null && product.getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal finalPrice = product.getGiaBan().subtract(product.getKhuyenMai());
                    result.append(" (Giảm ").append(formatPrice(product.getKhuyenMai()))
                          .append(", còn ").append(formatPrice(finalPrice)).append(")");
                }
                result.append("\n");
            }
            
            if (product.getLoai() != null) {
                result.append("📁 Danh mục: ").append(product.getLoai().getTen()).append("\n");
            }
            
            if (product.getNhanHieu() != null) {
                result.append("🏷️ Thương hiệu: ").append(product.getNhanHieu().getTen()).append("\n");
            }
            
            if (product.getGioiTinh() != null) {
                String gender = product.getGioiTinh() == 1 ? "Nam" : 
                               product.getGioiTinh() == 2 ? "Nữ" : "Unisex";
                result.append("👤 Giới tính: ").append(gender).append("\n");
            }
            
            if (product.getMoTa() != null && !product.getMoTa().trim().isEmpty()) {
                String shortDesc = product.getMoTa().length() > 80 ? 
                    product.getMoTa().substring(0, 80) + "..." : product.getMoTa();
                result.append("📝 Mô tả: ").append(shortDesc).append("\n");
            }
            
            result.append("🔗 Xem chi tiết: /product-details/").append(product.getId()).append("\n");
            result.append("\n");
            count++;
        }
        
        if (products.size() > limit) {
            result.append("... và ").append(products.size() - limit).append(" sản phẩm khác\n");
        }
        
        return result.toString();
    }
    
    private String formatProductDetails(SanPham product) {
        StringBuilder result = new StringBuilder();
        result.append("🛍️ ").append(product.getTen()).append("\n");
        result.append("═══════════════════════════════════════\n");
        
        if (product.getGiaBan() != null) {
            result.append("💰 Giá bán: ").append(formatPrice(product.getGiaBan())).append("\n");
        }
        
        if (product.getKhuyenMai() != null && product.getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal finalPrice = product.getGiaBan().subtract(product.getKhuyenMai());
            result.append("🎉 Khuyến mại: ").append(formatPrice(product.getKhuyenMai())).append("\n");
            result.append("💵 Giá cuối: ").append(formatPrice(finalPrice)).append("\n");
        }
        
        if (product.getLoai() != null) {
            result.append("📁 Danh mục: ").append(product.getLoai().getTen()).append("\n");
        }
        
        if (product.getNhanHieu() != null) {
            result.append("🏷️ Thương hiệu: ").append(product.getNhanHieu().getTen()).append("\n");
        }
        
        if (product.getGioiTinh() != null) {
            String gender = product.getGioiTinh() == 1 ? "Nam" : 
                           product.getGioiTinh() == 2 ? "Nữ" : "Unisex";
            result.append("👤 Giới tính: ").append(gender).append("\n");
        }
        
        if (product.getMoTa() != null && !product.getMoTa().trim().isEmpty()) {
            result.append("📝 Mô tả: ").append(product.getMoTa()).append("\n");
        }
        
        if (product.getThanhPhan() != null && !product.getThanhPhan().trim().isEmpty()) {
            result.append("🧵 Thành phần: ").append(product.getThanhPhan()).append("\n");
        }
        
        if (product.getHuongDan() != null && !product.getHuongDan().trim().isEmpty()) {
            result.append("📋 Hướng dẫn: ").append(product.getHuongDan()).append("\n");
        }
        
        if (product.getTag() != null && !product.getTag().trim().isEmpty()) {
            result.append("🏷️ Tags: ").append(product.getTag()).append("\n");
        }
        
        // Lấy thông tin biến thể (màu sắc, size)
        if (product.getBienThes() != null && !product.getBienThes().isEmpty()) {
            result.append("🎨 Màu sắc có sẵn: ");
            product.getBienThes().stream()
                .map(bienThe -> bienThe.getMauSac().getMaMau())
                .distinct()
                .forEach(color -> result.append(color).append(" "));
            result.append("\n");
            
            result.append("📏 Size có sẵn: ");
            product.getBienThes().stream()
                .map(bienThe -> bienThe.getSize().getTenSize())
                .distinct()
                .forEach(size -> result.append(size).append(" "));
            result.append("\n");
        }
        
        result.append("🔗 Xem chi tiết: /product-details/").append(product.getId()).append("\n");
        
        return result.toString();
    }
    
    private String extractBrandName(String query) {
        String[] brandKeywords = {
            "nike", "adidas", "puma", "converse", "vans", "new balance", 
            "reebok", "under armour", "champion", "uniqlo", "zara", "h&m"
        };
        
        String lowerQuery = query.toLowerCase();
        for (String brand : brandKeywords) {
            if (lowerQuery.contains(brand)) {
                return brand;
            }
        }
        return "";
    }
    
    private String extractColorName(String query) {
        String[] colorKeywords = {
            "đỏ", "xanh", "vàng", "tím", "hồng", "cam", "nâu", "xám", "trắng", "đen",
            "red", "blue", "yellow", "purple", "pink", "orange", "brown", "gray", "white", "black"
        };
        
        String lowerQuery = query.toLowerCase();
        for (String color : colorKeywords) {
            if (lowerQuery.contains(color)) {
                return color;
            }
        }
        return "";
    }
    
    private String extractSizeName(String query) {
        String[] sizeKeywords = {
            "xs", "s", "m", "l", "xl", "xxl", "xxxl",
            "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46"
        };
        
        String lowerQuery = query.toLowerCase();
        for (String size : sizeKeywords) {
            if (lowerQuery.contains(size)) {
                return size;
            }
        }
        return "";
    }
    
    private Integer extractGender(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("nam") || lowerQuery.contains("male") || lowerQuery.contains("men")) {
            return 1;
        } else if (lowerQuery.contains("nữ") || lowerQuery.contains("female") || lowerQuery.contains("women")) {
            return 2;
        }
        return null;
    }
    
    private String extractTagName(String query) {
        String[] tagKeywords = {
            "sale", "new", "hot", "trending", "best", "popular", "limited", "exclusive"
        };
        
        String lowerQuery = query.toLowerCase();
        for (String tag : tagKeywords) {
            if (lowerQuery.contains(tag)) {
                return tag;
            }
        }
        return "";
    }
    
    private List<SanPham> findProductsByBrand(String brandName) {
        return sanPhamRepository.findByNhanHieuTenContainingIgnoreCaseAndTrangThaiHoatDong(brandName, true);
    }
    
    private List<SanPham> findProductsByColor(String colorName) {
        return sanPhamRepository.findByBienThesMauSacMaMauContainingIgnoreCaseAndTrangThaiHoatDong(colorName, true);
    }
    
    private List<SanPham> findProductsBySize(String sizeName) {
        return sanPhamRepository.findByBienThesSizeTenSizeContainingIgnoreCaseAndTrangThaiHoatDong(sizeName, true);
    }
    
    private List<SanPham> findProductsByTag(String tagName) {
        return sanPhamRepository.findByTagContainingIgnoreCaseAndTrangThaiHoatDong(tagName, true);
    }
    
    private String extractCategoryName(String query) {
        String lowerQuery = query.toLowerCase();
        
        // Tìm tên danh mục trong câu hỏi
        String[] categoryKeywords = {
            "áo thun", "quần jean", "giày", "túi", "phụ kiện", "áo sơ mi", "quần short", 
            "váy", "đầm", "áo khoác", "quần tây", "giày thể thao", "túi xách", 
            "mũ", "kính", "đồng hồ", "thắt lưng", "ví", "balo", "giày sneaker",
            "áo polo", "quần jogger", "áo hoodie", "quần legging", "áo tank top"
        };
        
        // Tìm từ khóa danh mục trong câu hỏi
        for (String keyword : categoryKeywords) {
            if (lowerQuery.contains(keyword)) {
                return keyword;
            }
        }
        
        // Tìm từ sau "danh mục" hoặc "category"
        if (lowerQuery.contains("danh mục")) {
            String[] parts = query.split("danh mục");
            if (parts.length > 1) {
                String afterCategory = parts[1].trim();
                // Loại bỏ các từ không cần thiết
                afterCategory = afterCategory.replaceAll("\\b(tất cả|sản phẩm|của|trong|nào|có|gì)\\b", "").trim();
                String[] words = afterCategory.split("\\s+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    return words[0];
                }
            }
        }
        
        if (lowerQuery.contains("category")) {
            String[] parts = query.split("category");
            if (parts.length > 1) {
                String afterCategory = parts[1].trim();
                // Loại bỏ các từ không cần thiết
                afterCategory = afterCategory.replaceAll("\\b(all|products|of|in|what|have|is)\\b", "").trim();
                String[] words = afterCategory.split("\\s+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    return words[0];
                }
            }
        }
        
        // Tìm từ sau "sản phẩm"
        if (lowerQuery.contains("sản phẩm")) {
            String[] parts = query.split("sản phẩm");
            if (parts.length > 1) {
                String afterProduct = parts[1].trim();
                // Loại bỏ các từ không cần thiết
                afterProduct = afterProduct.replaceAll("\\b(tất cả|danh mục|của|trong|nào|có|gì)\\b", "").trim();
                String[] words = afterProduct.split("\\s+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    return words[0];
                }
            }
        }
        
        return "";
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
