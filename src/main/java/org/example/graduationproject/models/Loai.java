package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Loai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String ten;

    @OneToMany(mappedBy = "loai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SanPham> sanPhams;

    @OneToMany(mappedBy = "loai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MauSac> mauSacs;

    @OneToMany(mappedBy = "loai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Size> sizes;
}

