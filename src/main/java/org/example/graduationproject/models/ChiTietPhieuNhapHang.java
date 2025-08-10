package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietPhieuNhapHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuNhapHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer soLuongNhap;
    private BigDecimal thanhTienNhap;

    @ManyToOne
    @JoinColumn(name = "Id_PhieuNhap")
    private PhieuNhapHang phieuNhapHang;

    @ManyToOne
    @JoinColumn(name = "Id_SanPhamBienThe")
    private SanPhamBienThe sanPhamBienThe;
}