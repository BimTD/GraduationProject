package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietGioHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietGioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Id_GioHang")
    private GioHang gioHang;

    @ManyToOne
    @JoinColumn(name = "Id_SanPhamBienThe")
    private SanPhamBienThe sanPhamBienThe;

    private Integer soLuong;
    private BigDecimal giaBan;
    private BigDecimal thanhTien;
}
