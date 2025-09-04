package org.example.graduationproject.dto;

import java.util.List;

public class ProductResponseDTO {
    private boolean success;
    private String message;
    private Object data;

    public ProductResponseDTO() {}

    public ProductResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ProductResponseDTO(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
