package org.example.graduationproject.controllers.user;

import org.example.graduationproject.dto.ProductResponseDTO;
import org.example.graduationproject.services.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserProductApiController {

    @Autowired
    private UserProductService userProductService;

    @GetMapping("/products/{id}/quick-view")
    public ResponseEntity<ProductResponseDTO> getQuickView(@PathVariable("id") Integer productId) {
        ProductResponseDTO response = userProductService.getProductQuickViewWithValidation(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}/related")
    public ResponseEntity<ProductResponseDTO> getRelatedProducts(@PathVariable("id") Integer productId, 
                                                               @RequestParam(defaultValue = "8") int limit) {
        ProductResponseDTO response = userProductService.getRelatedProductsWithValidation(productId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}/upsell")
    public ResponseEntity<ProductResponseDTO> getUpsellProducts(@PathVariable("id") Integer productId, 
                                                              @RequestParam(defaultValue = "8") int limit) {
        ProductResponseDTO response = userProductService.getUpsellProductsWithValidation(productId, limit);
        return ResponseEntity.ok(response);
    }
}




