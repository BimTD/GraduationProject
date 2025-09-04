package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    
    public static OrderResponseDTO success(String message, Object data) {
        return new OrderResponseDTO(true, message, data);
    }
    
    public static OrderResponseDTO success(String message) {
        return new OrderResponseDTO(true, message, null);
    }
    
    public static OrderResponseDTO error(String message) {
        return new OrderResponseDTO(false, message, null);
    }
    
    public static OrderResponseDTO error(String message, Object data) {
        return new OrderResponseDTO(false, message, data);
    }
}
