package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonBackReference
    private User user;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    @Column(columnDefinition = "VARCHAR(50)")
    private String trangThai; // "active", "ordered", "abandoned"

    @Column(name = "email_sent", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailSent = false; // Đánh dấu đã gửi email thông báo

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt; // Thời điểm gửi email thông báo

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private java.util.List<ChiTietGioHang> chiTietGioHangs;
}
