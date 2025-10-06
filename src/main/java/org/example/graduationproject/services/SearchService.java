package org.example.graduationproject.services;

import org.example.graduationproject.dto.SearchResponseDTO;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {
    
    /**
     * Lấy gợi ý tìm kiếm (autocomplete)
     */
    SearchResponseDTO getSearchSuggestions(String query, int limit);
    
    /**
     * Tìm kiếm nhanh
     */
    SearchResponseDTO quickSearch(String query, Integer categoryId, int limit);
    
    /**
     * Lấy danh sách tìm kiếm phổ biến
     */
    SearchResponseDTO getPopularSearches(int limit);
    
    /**
     * Tìm kiếm nâng cao với filters
     */
    SearchResponseDTO advancedSearch(String query, Integer categoryId, Integer brandId, 
                                   Integer colorId, String gender, BigDecimal minPrice, 
                                   BigDecimal maxPrice, String tag, String sort, 
                                   int page, int size);
    
    /**
     * Lưu lịch sử tìm kiếm
     */
    SearchResponseDTO saveSearchHistory(String query);
    
    /**
     * Lấy lịch sử tìm kiếm
     */
    SearchResponseDTO getSearchHistory(int limit);
    
    /**
     * Lấy danh sách sản phẩm bán chạy nhất
     */
    SearchResponseDTO getBestsellingProducts(int limit);
}
