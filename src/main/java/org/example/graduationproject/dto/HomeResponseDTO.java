package org.example.graduationproject.dto;

import java.util.List;
import java.util.Map;

public class HomeResponseDTO {
    private boolean success;
    private String message;
    private Map<String, List<Object>> sanPhamNamTheoLoai;
    private Map<String, List<Object>> sanPhamNuTheoLoai;
    private List<Object> products;

    public HomeResponseDTO() {}

    public HomeResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public HomeResponseDTO(boolean success, String message, Map<String, List<Object>> sanPhamNamTheoLoai, 
                          Map<String, List<Object>> sanPhamNuTheoLoai, List<Object> products) {
        this.success = success;
        this.message = message;
        this.sanPhamNamTheoLoai = sanPhamNamTheoLoai;
        this.sanPhamNuTheoLoai = sanPhamNuTheoLoai;
        this.products = products;
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

    public Map<String, List<Object>> getSanPhamNamTheoLoai() {
        return sanPhamNamTheoLoai;
    }

    public void setSanPhamNamTheoLoai(Map<String, List<Object>> sanPhamNamTheoLoai) {
        this.sanPhamNamTheoLoai = sanPhamNamTheoLoai;
    }

    public Map<String, List<Object>> getSanPhamNuTheoLoai() {
        return sanPhamNuTheoLoai;
    }

    public void setSanPhamNuTheoLoai(Map<String, List<Object>> sanPhamNuTheoLoai) {
        this.sanPhamNuTheoLoai = sanPhamNuTheoLoai;
    }

    public List<Object> getProducts() {
        return products;
    }

    public void setProducts(List<Object> products) {
        this.products = products;
    }
}
