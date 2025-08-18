package org.example.graduationproject.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "NhanHieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String ten;
    
    private LocalDateTime dateCreate;

    @OneToMany(mappedBy = "nhanHieu")
    private List<SanPham> sanPhams;
}

