package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_su_dung_ma_giam_gia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichSuSuDungMaGiamGia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_giam_gia_id", nullable = false)
    private MaGiamGia maGiamGia;
    
    @Column(name = "ma_giam_gia_code", nullable = false)
    private String maGiamGiaCode;
    
    @Column(name = "thoi_gian_su_dung", nullable = false)
    private LocalDateTime thoiGianSuDung;
    
    @Column(name = "gia_tri_giam_gia", nullable = false)
    private java.math.BigDecimal giaTriGiamGia;
    
    @Column(name = "don_hang_id")
    private Long donHangId; // ID của đơn hàng sử dụng mã này
    
    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "USED"; // USED, CANCELLED
    
    // Constructor tiện ích
    public LichSuSuDungMaGiamGia(User user, MaGiamGia maGiamGia, java.math.BigDecimal giaTriGiamGia, Long donHangId) {
        this.user = user;
        this.maGiamGia = maGiamGia;
        this.maGiamGiaCode = maGiamGia.getMaGiamGia();
        this.giaTriGiamGia = giaTriGiamGia;
        this.donHangId = donHangId;
        this.thoiGianSuDung = LocalDateTime.now();
        this.trangThai = "USED";
    }
}

