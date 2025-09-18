package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "MaGiamGia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_giam_gia", unique = true, columnDefinition = "VARCHAR(50)")
    private String maGiamGia; // Mã code để người dùng nhập

    @Column(name = "ten_ma_giam_gia", columnDefinition = "VARCHAR(255)")
    private String tenMaGiamGia; // Tên mô tả mã giảm giá

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa; // Mô tả chi tiết

    @Column(name = "loai_giam_gia", columnDefinition = "VARCHAR(20)")
    private String loaiGiamGia; // "PERCENTAGE" (phần trăm) hoặc "FIXED" (số tiền cố định)

    @Column(name = "gia_tri_giam_gia", precision = 18, scale = 2)
    private BigDecimal giaTriGiamGia; // Giá trị giảm (phần trăm hoặc số tiền)

    @Column(name = "gia_tri_toi_thieu", precision = 18, scale = 2)
    private BigDecimal giaTriToiThieu; // Giá trị đơn hàng tối thiểu để áp dụng

    @Column(name = "gia_tri_toi_da", precision = 18, scale = 2)
    private BigDecimal giaTriToiDa; // Giá trị giảm tối đa (chỉ áp dụng với loại PERCENTAGE)

    @Column(name = "so_luong_su_dung")
    private Integer soLuongSuDung; // Số lần sử dụng tối đa

    @Column(name = "so_luong_da_su_dung")
    private Integer soLuongDaSuDung = 0; // Số lần đã sử dụng

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau; // Ngày bắt đầu có hiệu lực

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc; // Ngày kết thúc

    @Column(name = "trang_thai", columnDefinition = "VARCHAR(20)")
    private String trangThai = "ACTIVE"; // "ACTIVE", "INACTIVE", "EXPIRED"

    @Column(name = "ap_dung_cho_tat_ca")
    private Boolean apDungChoTatCa = true; // Có áp dụng cho tất cả sản phẩm không

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    // Quan hệ với sản phẩm (nếu không áp dụng cho tất cả)
    @ManyToMany
    @JoinTable(
        name = "MaGiamGia_SanPham",
        joinColumns = @JoinColumn(name = "ma_giam_gia_id"),
        inverseJoinColumns = @JoinColumn(name = "san_pham_id")
    )
    @JsonManagedReference
    private List<SanPham> sanPhams; // Danh sách sản phẩm được áp dụng

    // Quan hệ với hóa đơn đã sử dụng mã này
    @OneToMany(mappedBy = "maGiamGia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDon> hoaDons; // Danh sách hóa đơn đã sử dụng mã này

    // Constructor tiện ích
    public MaGiamGia(String maGiamGia, String tenMaGiamGia, String loaiGiamGia, 
                     BigDecimal giaTriGiamGia, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        this.maGiamGia = maGiamGia;
        this.tenMaGiamGia = tenMaGiamGia;
        this.loaiGiamGia = loaiGiamGia;
        this.giaTriGiamGia = giaTriGiamGia;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
        this.trangThai = "ACTIVE";
        this.soLuongDaSuDung = 0;
        this.apDungChoTatCa = true;
    }

    // Phương thức kiểm tra mã có còn hiệu lực không
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(this.trangThai) 
            && now.isAfter(this.ngayBatDau) 
            && now.isBefore(this.ngayKetThuc)
            && (this.soLuongSuDung == null || this.soLuongDaSuDung < this.soLuongSuDung);
    }

    // Phương thức kiểm tra có thể áp dụng cho sản phẩm không
    public boolean canApplyToProduct(SanPham sanPham) {
        if (this.apDungChoTatCa) {
            return true;
        }
        return this.sanPhams != null && this.sanPhams.contains(sanPham);
    }

    // Phương thức tính giá trị giảm giá
    public BigDecimal calculateDiscountAmount(BigDecimal orderTotal) {
        if (!isActive() || orderTotal.compareTo(this.giaTriToiThieu) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount;
        if ("PERCENTAGE".equals(this.loaiGiamGia)) {
            discountAmount = orderTotal.multiply(this.giaTriGiamGia).divide(new BigDecimal("100"));
            // Áp dụng giới hạn tối đa nếu có
            if (this.giaTriToiDa != null && discountAmount.compareTo(this.giaTriToiDa) > 0) {
                discountAmount = this.giaTriToiDa;
            }
        } else { // FIXED
            discountAmount = this.giaTriGiamGia;
        }

        // Không được giảm nhiều hơn tổng tiền đơn hàng
        return discountAmount.min(orderTotal);
    }
}
