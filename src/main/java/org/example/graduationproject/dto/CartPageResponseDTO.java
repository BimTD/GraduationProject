package org.example.graduationproject.dto;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;

public class CartPageResponseDTO {
    private boolean success;
    private String message;
    private GioHang cart;
    private User user;

    public CartPageResponseDTO() {}

    public CartPageResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CartPageResponseDTO(boolean success, String message, GioHang cart, User user) {
        this.success = success;
        this.message = message;
        this.cart = cart;
        this.user = user;
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

    public GioHang getCart() {
        return cart;
    }

    public void setCart(GioHang cart) {
        this.cart = cart;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

