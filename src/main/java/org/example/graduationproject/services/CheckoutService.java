package org.example.graduationproject.services;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;

public interface CheckoutService {
    
    // Business logic methods for controller
    CheckoutResponseDTO getCheckoutPageDataWithValidation();
    CheckoutResponseDTO processCheckoutWithValidation(CheckoutDTO checkoutDTO);
}


