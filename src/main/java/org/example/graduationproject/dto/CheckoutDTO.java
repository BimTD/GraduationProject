package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO {
    
    // Thông tin cơ bản
    private String ghiChu;
    private String loaiThanhToan; // "CASH", "BANK_TRANSFER", "CREDIT_CARD"
    private String diaChiGiaoHang;
    private String soDienThoai;
    private String tenNguoiNhan;
    private BigDecimal phiGiaoHang;
    private BigDecimal tongTien;
    
    // Thông tin mã giảm giá
    private String maGiamGia; // Mã giảm giá người dùng nhập
    private BigDecimal giaTriGiamGia = BigDecimal.ZERO; // Giá trị giảm giá được áp dụng
    private BigDecimal tongTienSauGiamGia; // Tổng tiền sau khi giảm giá
    
    // Thông tin bổ sung từ frontend (có thể ignore nếu không cần)
    private String ho;
    private String ten;
    private String tenCongTy;
    private String quocGia;
    private String diaChi1;
    private String diaChi2;
    private String thanhPho;
    private String tinhHuyen;
    private String email;
}
