package org.example.graduationproject.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequestDTO {
    private Integer supplierId;
    private String note;
    private List<ImportDetailDTO> details;
} 