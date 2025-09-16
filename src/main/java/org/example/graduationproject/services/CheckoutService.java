package org.example.graduationproject.services;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;
import org.example.graduationproject.models.HoaDon;

public interface CheckoutService {
    
    // Business logic methods for controller
    CheckoutResponseDTO getCheckoutPageDataWithValidation();
    CheckoutResponseDTO processCheckoutWithValidation(CheckoutDTO checkoutDTO);
    CheckoutResponseDTO validateMaGiamGia(String maGiamGia);
    
    // Method to get HoaDon by ID
    HoaDon getHoaDonById(Integer orderId);
}


