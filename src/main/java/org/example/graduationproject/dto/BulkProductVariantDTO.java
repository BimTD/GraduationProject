package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkProductVariantDTO {
    private Integer sanPhamId;
    private List<Integer> mauSacIds;
    private List<Integer> sizeIds;
    private Integer soLuongTon;
    
    // Thêm các trường để lưu thông tin hiển thị
    private String sanPhamTen;
    private List<MauSacInfo> availableColors;
    private List<SizeInfo> availableSizes;
    
    // Inner classes để lưu thông tin màu sắc và size
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MauSacInfo {
        private Integer id;
        private String maMau;
        private String loaiTen;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeInfo {
        private Integer id;
        private String tenSize;
        private String loaiTen;
    }
    
    // DTO mới để trả về kết quả tạo biến thể hàng loạt
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkCreateResult {
        private int totalRequested; // Tổng số biến thể được yêu cầu tạo
        private int createdNew; // Số biến thể được tạo mới
        private int alreadyExists; // Số biến thể đã tồn tại
        private List<String> createdVariants; // Danh sách biến thể được tạo mới
        private List<String> existingVariants; // Danh sách biến thể đã tồn tại
        private String message; // Thông báo tổng hợp
    }
}
