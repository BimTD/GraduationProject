package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(columnDefinition = "VARCHAR(255)")
    private String ten;
    
    @Column(columnDefinition = "VARCHAR(255)")
    private String email;
    
    @Column(columnDefinition = "VARCHAR(20)")
    private String sdt;
    
    @Column(columnDefinition = "TEXT")
    private String thongTin;
    
    @Column(columnDefinition = "VARCHAR(500)")
    private String diaChi;

    @OneToMany(mappedBy = "nhaCungCap")
    @JsonIgnore
    private List<SanPham> sanPhams;
}

