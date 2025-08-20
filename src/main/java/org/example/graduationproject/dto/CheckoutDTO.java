package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO {
    
    private String ghiChu;
    private String loaiThanhToan; // "CASH", "BANK_TRANSFER", "CREDIT_CARD"
    private String diaChiGiaoHang;
    private String soDienThoai;
    private String tenNguoiNhan;
    private BigDecimal phiGiaoHang;
    private BigDecimal tongTien;
}
