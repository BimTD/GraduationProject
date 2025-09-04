package org.example.graduationproject.controllers.user;

import org.example.graduationproject.dto.AddToCartDTO;
import org.example.graduationproject.dto.CartResponseDTO;
import org.example.graduationproject.dto.RemoveCartItemDTO;
import org.example.graduationproject.dto.UpdateCartItemDTO;
import org.example.graduationproject.services.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private GioHangService gioHangService;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestBody AddToCartDTO addToCartDTO) {
        CartResponseDTO response = gioHangService.addToCartWithValidation(addToCartDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<CartResponseDTO> getCartItemCount() {
        CartResponseDTO response = gioHangService.getCartItemCount();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-quantity")
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(@RequestBody UpdateCartItemDTO updateCartItemDTO) {
        CartResponseDTO response = gioHangService.updateCartItemQuantityWithValidation(updateCartItemDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    public ResponseEntity<CartResponseDTO> removeFromCart(@RequestBody RemoveCartItemDTO removeCartItemDTO) {
        CartResponseDTO response = gioHangService.removeFromCartWithValidation(removeCartItemDTO);
        return ResponseEntity.ok(response);
    }
}
