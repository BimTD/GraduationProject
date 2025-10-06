package org.example.graduationproject.dto;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;

public class CheckoutResponseDTO {
    private boolean success;
    private String message;
    private GioHang cart;
    private User user;
    private HoaDon order;
    private Integer orderId;

    public CheckoutResponseDTO() {}

    public CheckoutResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CheckoutResponseDTO(boolean success, String message, GioHang cart, User user) {
        this.success = success;
        this.message = message;
        this.cart = cart;
        this.user = user;
    }

    public CheckoutResponseDTO(boolean success, String message, HoaDon order) {
        this.success = success;
        this.message = message;
        this.order = order;
        this.orderId = order != null ? order.getId() : null;
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

    public HoaDon getOrder() {
        return order;
    }

    public void setOrder(HoaDon order) {
        this.order = order;
        this.orderId = order != null ? order.getId() : null;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
}



































































