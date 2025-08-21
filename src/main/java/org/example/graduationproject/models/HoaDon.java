package org.example.graduationproject.models;

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

    @ManyToOne
    @JoinColumn(name = "Id_User")
    private User user;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietHoaDon> chiTietHoaDons;
}




