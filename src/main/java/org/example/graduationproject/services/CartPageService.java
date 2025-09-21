package org.example.graduationproject.services;

import org.example.graduationproject.dto.CartPageResponseDTO;

public interface CartPageService {
    
    // Business logic methods for controller
    CartPageResponseDTO getCartPageDataWithValidation();
    
    // Kiểm tra thời gian hết hạn thanh toán
    boolean isCartPaymentExpired();
}


