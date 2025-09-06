package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.graduationproject.models.SanPham;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private boolean success;
    private String message;
    private Page<SanPham> products;
    private List<Object> suggestions;
    private List<String> popularSearches;
    private List<String> searchHistory;
    private long totalResults;
    private int currentPage;
    private int totalPages;

    // Constructor for success response with products
    public SearchResponseDTO(boolean success, String message, Page<SanPham> products) {
        this.success = success;
        this.message = message;
        this.products = products;
        if (products != null) {
            this.totalResults = products.getTotalElements();
            this.currentPage = products.getNumber();
            this.totalPages = products.getTotalPages();
        }
    }

    // Constructor for success response with suggestions
    public SearchResponseDTO(boolean success, String message, List<Object> suggestions) {
        this.success = success;
        this.message = message;
        this.suggestions = suggestions;
    }

    // Constructor for success response with popular searches
    public SearchResponseDTO(boolean success, String message, List<String> popularSearches, boolean isPopular) {
        this.success = success;
        this.message = message;
        this.popularSearches = popularSearches;
    }

    // Constructor for success response with search history
    public SearchResponseDTO(boolean success, String message, List<String> searchHistory, int dummy) {
        this.success = success;
        this.message = message;
        this.searchHistory = searchHistory;
    }

    // Constructor for error response
    public SearchResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
