package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    
    public static CartResponseDTO success(String message) {
        return new CartResponseDTO(true, message, null);
    }
    
    public static CartResponseDTO success(String message, Object data) {
        return new CartResponseDTO(true, message, data);
    }
    
    public static CartResponseDTO error(String message) {
        return new CartResponseDTO(false, message, null);
    }
}

