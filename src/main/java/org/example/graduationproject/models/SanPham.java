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
@Table(name = "SanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String ten;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String moTa;
    
    private BigDecimal giaBan;
    private BigDecimal giaNhap;
    private BigDecimal khuyenMai;
    
    @Column(columnDefinition = "NVARCHAR(255)")
    private String tag;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String huongDan;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String thanhPhan;
    
    private LocalDateTime ngayCapNhat;
    private LocalDateTime ngayTao;
    
    @Column(columnDefinition = "NVARCHAR(50)")
    private String trangThaiSanPham;
    
    private Boolean trangThaiHoatDong;
    private Integer gioiTinh;

    @ManyToOne
    @JoinColumn(name = "Id_Loai")
    private Loai loai;

    @ManyToOne
    @JoinColumn(name = "Id_NhanHieu")
    private NhanHieu nhanHieu;

    @ManyToOne
    @JoinColumn(name = "Id_NhaCungCap")
    private NhaCungCap nhaCungCap;

    @OneToMany(mappedBy = "sanPham")
    private List<ImageSanPham> images;

    @OneToMany(mappedBy = "sanPham")
    private List<SanPhamBienThe> bienThes;
}

