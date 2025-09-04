package org.example.graduationproject.services;

import org.example.graduationproject.dto.AddToCartDTO;
import org.example.graduationproject.dto.CartResponseDTO;
import org.example.graduationproject.dto.RemoveCartItemDTO;
import org.example.graduationproject.dto.UpdateCartItemDTO;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;

public interface GioHangService {
    GioHang getOrCreateActiveCart(User user);
    boolean addToCart(User user, AddToCartDTO addToCartDTO);
    boolean updateCartItemQuantity(User user, Integer cartItemId, Integer quantity);
    boolean removeFromCart(User user, Integer cartItemId);
    GioHang getActiveCart(User user);
    boolean updateCartStatus(GioHang gioHang);
    
    // Business logic methods for controller
    CartResponseDTO addToCartWithValidation(AddToCartDTO addToCartDTO);
    CartResponseDTO updateCartItemQuantityWithValidation(UpdateCartItemDTO updateCartItemDTO);
    CartResponseDTO removeFromCartWithValidation(RemoveCartItemDTO removeCartItemDTO);
    CartResponseDTO getCartItemCount();
}
