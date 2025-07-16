package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SanPhamBienThe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamBienThe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer soLuongTon;

    @ManyToOne
    @JoinColumn(name = "Id_SanPham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "Id_Mau")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "SizeId")
    private Size size;
}
