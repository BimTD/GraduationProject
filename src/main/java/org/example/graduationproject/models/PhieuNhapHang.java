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
@Table(name = "PhieuNhapHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhapHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String soChungTu;
    private LocalDateTime ngayTao;
    private BigDecimal tongTien;
    private String nguoiLapPhieu;
    private String ghiChu;

    @ManyToOne
    @JoinColumn(name = "Id_NhaCungCap")
    private NhaCungCap nhaCungCap;

    @OneToMany(mappedBy = "phieuNhapHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietPhieuNhapHang> chiTietPhieuNhaps;
}