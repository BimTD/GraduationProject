package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaGiamGiaDTO {
    private Integer id;
    private String maGiamGia;
    private String tenMaGiamGia;
    private String moTa;
    private String loaiGiamGia;
    private BigDecimal giaTriGiamGia;
    private BigDecimal giaTriToiThieu;
    private BigDecimal giaTriToiDa;
    private Integer soLuongSuDung;
    private Integer soLuongDaSuDung;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String trangThai;
    private Boolean apDungChoTatCa;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    
    // Constructor từ entity
    public MaGiamGiaDTO(org.example.graduationproject.models.MaGiamGia entity) {
        this.id = entity.getId();
        this.maGiamGia = entity.getMaGiamGia();
        this.tenMaGiamGia = entity.getTenMaGiamGia();
        this.moTa = entity.getMoTa();
        this.loaiGiamGia = entity.getLoaiGiamGia();
        this.giaTriGiamGia = entity.getGiaTriGiamGia();
        this.giaTriToiThieu = entity.getGiaTriToiThieu();
        this.giaTriToiDa = entity.getGiaTriToiDa();
        this.soLuongSuDung = entity.getSoLuongSuDung();
        this.soLuongDaSuDung = entity.getSoLuongDaSuDung();
        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.trangThai = entity.getTrangThai();
        this.apDungChoTatCa = entity.getApDungChoTatCa();
        this.ngayTao = entity.getNgayTao();
        this.ngayCapNhat = entity.getNgayCapNhat();
    }
}
