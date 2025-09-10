package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "HoaDon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime ngayTao;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String trangThai;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String loaiThanhToan;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String daLayTien;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String diaChiGiaoHang;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String tenNguoiNhan;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String soDienThoaiGiaoHang;

    private BigDecimal tongTien;

    // Thông tin mã giảm giá
    @Column(name = "ma_giam_gia_su_dung", columnDefinition = "NVARCHAR(50)")
    private String maGiamGiaSuDung; // Mã giảm giá đã sử dụng

    @Column(name = "gia_tri_giam_gia", precision = 18, scale = 2)
    private BigDecimal giaTriGiamGia = BigDecimal.ZERO; // Giá trị giảm giá đã áp dụng

    @Column(name = "tong_tien_sau_giam_gia", precision = 18, scale = 2)
    private BigDecimal tongTienSauGiamGia; // Tổng tiền sau khi giảm giá

    @ManyToOne
    @JoinColumn(name = "Id_User")
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "Id_MaGiamGia")
    @JsonBackReference
    private MaGiamGia maGiamGia; // Quan hệ với mã giảm giá

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ChiTietHoaDon> chiTietHoaDons;
    
    // Các field tạm thời để lưu lịch sử sử dụng mã giảm giá (không lưu vào DB)
    @Transient
    private List<MaGiamGia> maGiamGiaList;
    
    @Transient
    private List<BigDecimal> giaTriGiamGiaList;
}




