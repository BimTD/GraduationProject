package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Size")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARCHAR(50)")
    private String tenSize;

    @ManyToOne
    @JoinColumn(name = "Id_Loai")
    private Loai loai;

    @OneToMany(mappedBy = "size")
    @JsonIgnore
    private List<SanPhamBienThe> sanPhamBienThes;
}

