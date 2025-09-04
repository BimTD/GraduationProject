package org.example.graduationproject.services;

import org.example.graduationproject.dto.HomeResponseDTO;

public interface HomeService {
    
    // Business logic methods for controller
    HomeResponseDTO getHomePageDataWithValidation();
}

