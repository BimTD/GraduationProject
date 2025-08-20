package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartDTO {
    private Integer productId;
    private Integer sizeId;
    private Integer colorId;
    private Integer quantity;
}
