package org.example.graduationproject.services;

import org.example.graduationproject.dto.ShopResponseDTO;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface ShopService {
    ShopResponseDTO getShopPageDataWithValidation(String search, Integer categoryId, String gender, 
                                                 Integer colorId, String tag, Integer brandId, 
                                                 BigDecimal minPrice, BigDecimal maxPrice, String sort, int page, int size);
}
