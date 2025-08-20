package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "GioHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Id_User")
    private User user;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String trangThai; // "active", "ordered", "abandoned"

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<ChiTietGioHang> chiTietGioHangs;
}
