package org.example.graduationproject.services;

import org.example.graduationproject.dto.ProductResponseDTO;

public interface UserProductService {
    
    // Business logic methods for controller
    ProductResponseDTO getProductQuickViewWithValidation(Integer productId);
    ProductResponseDTO getProductDetailsWithValidation(Integer productId);
    ProductResponseDTO getRelatedProductsWithValidation(Integer productId, int limit);
    ProductResponseDTO getUpsellProductsWithValidation(Integer productId, int limit);
}

