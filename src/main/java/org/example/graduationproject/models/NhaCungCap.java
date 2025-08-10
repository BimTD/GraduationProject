package org.example.graduationproject.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "NhaCungCap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhaCungCap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ten;
    private String email;
    private String sdt;
    private String thongTin;
    private String diaChi;

    @OneToMany(mappedBy = "nhaCungCap")
    private List<SanPham> sanPhams;
}

