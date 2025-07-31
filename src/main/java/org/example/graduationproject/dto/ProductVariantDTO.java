package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Integer id;
    private Integer soLuongTon;
    private Integer sanPhamId;
    private String sanPhamTen;
    private Integer mauSacId;
    private String mauSacTen;
    private Integer sizeId;
    private String sizeTen;
} 