package org.example.graduationproject.controllers.api;

import org.example.graduationproject.dto.SearchResponseDTO;
import org.example.graduationproject.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchApiController {

    @Autowired
    private SearchService searchService;

    /**
     * API tìm kiếm autocomplete - gợi ý sản phẩm khi gõ
     */
    @GetMapping("/suggestions")
    public ResponseEntity<SearchResponseDTO> getSearchSuggestions(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        
        try {
            SearchResponseDTO response = searchService.getSearchSuggestions(query, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi tìm kiếm: " + e.getMessage()));
        }
    }

    /**
     * API tìm kiếm nhanh - tìm kiếm real-time
     */
    @GetMapping("/quick")
    public ResponseEntity<SearchResponseDTO> quickSearch(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "limit", defaultValue = "12") int limit) {
        
        try {
            SearchResponseDTO response = searchService.quickSearch(query, categoryId, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi tìm kiếm nhanh: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách tìm kiếm phổ biến
     */
    @GetMapping("/popular")
    public ResponseEntity<SearchResponseDTO> getPopularSearches(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            SearchResponseDTO response = searchService.getPopularSearches(limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi lấy tìm kiếm phổ biến: " + e.getMessage()));
        }
    }

    /**
     * API tìm kiếm nâng cao với filters
     */
    @GetMapping("/advanced")
    public ResponseEntity<SearchResponseDTO> advancedSearch(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "colorId", required = false) Integer colorId,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {
        
        try {
            SearchResponseDTO response = searchService.advancedSearch(
                query, categoryId, brandId, colorId, gender, 
                minPrice, maxPrice, tag, sort, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi tìm kiếm nâng cao: " + e.getMessage()));
        }
    }

    /**
     * API lưu lịch sử tìm kiếm
     */
    @PostMapping("/history")
    public ResponseEntity<SearchResponseDTO> saveSearchHistory(
            @RequestParam("query") String query) {
        
        try {
            SearchResponseDTO response = searchService.saveSearchHistory(query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi lưu lịch sử: " + e.getMessage()));
        }
    }

    /**
     * API lấy lịch sử tìm kiếm của user
     */
    @GetMapping("/history")
    public ResponseEntity<SearchResponseDTO> getSearchHistory(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            SearchResponseDTO response = searchService.getSearchHistory(limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi lấy lịch sử: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách sản phẩm bán chạy nhất
     */
    @GetMapping("/bestsellers")
    public ResponseEntity<SearchResponseDTO> getBestsellingProducts(
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        
        try {
            SearchResponseDTO response = searchService.getBestsellingProducts(limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SearchResponseDTO(false, "Lỗi lấy sản phẩm bán chạy: " + e.getMessage()));
        }
    }
}
