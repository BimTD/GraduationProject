package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietHoaDon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer soLuong;

    private BigDecimal thanhTien;

    @ManyToOne
    @JoinColumn(name = "Id_HoaDon")
    @JsonBackReference
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "Id_SanPhamBienThe")
    private SanPhamBienThe sanPhamBienThe;
}




