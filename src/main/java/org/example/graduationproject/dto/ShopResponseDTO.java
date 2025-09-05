package org.example.graduationproject.dto;

import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.Loai;
import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.models.MauSac;
import org.springframework.data.domain.Page;

import java.util.List;

public class ShopResponseDTO {
    private boolean success;
    private String message;
    private Page<SanPham> products;
    private List<Loai> categories;
    private List<NhanHieu> brands;
    private List<MauSac> colors;
    private List<String> popularTags;
    private Integer totalProducts;

    public ShopResponseDTO() {}

    public ShopResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

        public ShopResponseDTO(boolean success, String message, Page<SanPham> products,
                          List<Loai> categories, List<NhanHieu> brands, List<MauSac> colors, 
                          List<String> popularTags) {
        this.success = success;
        this.message = message;
        this.products = products;
        this.categories = categories;
        this.brands = brands;
        this.colors = colors;
        this.popularTags = popularTags;
        this.totalProducts = (int) products.getTotalElements();
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

    public Page<SanPham> getProducts() {
        return products;
    }

    public void setProducts(Page<SanPham> products) {
        this.products = products;
    }

    public List<Loai> getCategories() {
        return categories;
    }

    public void setCategories(List<Loai> categories) {
        this.categories = categories;
    }

    public List<NhanHieu> getBrands() {
        return brands;
    }

    public void setBrands(List<NhanHieu> brands) {
        this.brands = brands;
    }

    public List<MauSac> getColors() {
        return colors;
    }

    public void setColors(List<MauSac> colors) {
        this.colors = colors;
    }

    public List<String> getPopularTags() {
        return popularTags;
    }

    public void setPopularTags(List<String> popularTags) {
        this.popularTags = popularTags;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }
}
