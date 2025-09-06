package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionDTO {
    private Integer id;
    private String ten;
    private String moTa;
    private String giaBan;
    private String imageUrl;
    private String categoryName;
    private String brandName;
}
