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

    @Column(columnDefinition = "NVARCHAR(255)")
    private String ten;
    
    @Column(columnDefinition = "NVARCHAR(255)")
    private String email;
    
    @Column(columnDefinition = "NVARCHAR(20)")
    private String sdt;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String thongTin;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String diaChi;

    @OneToMany(mappedBy = "nhaCungCap")
    private List<SanPham> sanPhams;
}

