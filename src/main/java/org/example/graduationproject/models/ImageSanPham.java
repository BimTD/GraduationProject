package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ImageSanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String imageName;

    @ManyToOne
    @JoinColumn(name = "IdSanPham")
    @JsonBackReference
    private SanPham sanPham;
}

