package org.example.graduationproject.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportDetailDTO {
    private Integer productId;
    private Integer variantId;
    private Integer quantity;
    private String importPrice;
} 