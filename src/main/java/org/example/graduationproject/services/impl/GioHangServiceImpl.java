package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.AddToCartDTO;
import org.example.graduationproject.dto.CartResponseDTO;
import org.example.graduationproject.dto.RemoveCartItemDTO;
import org.example.graduationproject.dto.UpdateCartItemDTO;
import org.example.graduationproject.exceptions.AuthenticationException;
import org.example.graduationproject.exceptions.ValidationException;
import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.ChiTietGioHangRepository;
import org.example.graduationproject.repositories.GioHangRepository;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.services.AuthenticationService;
import org.example.graduationproject.services.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GioHangServiceImpl implements GioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private ChiTietGioHangRepository chiTietGioHangRepository;

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public GioHang getOrCreateActiveCart(User user) {
        Optional<GioHang> existingCart = gioHangRepository.findByUserAndTrangThai(user, "active");
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Tạo giỏ hàng mới
        GioHang newCart = new GioHang();
        newCart.setUser(user);
        newCart.setTrangThai("active");
        newCart.setNgayTao(LocalDateTime.now());
        newCart.setNgayCapNhat(LocalDateTime.now());
        
        return gioHangRepository.save(newCart);
    }

    @Override
    public boolean addToCart(User user, AddToCartDTO addToCartDTO) {
        try {
            // Lấy hoặc tạo giỏ hàng
            GioHang cart = getOrCreateActiveCart(user);
            
            // Tìm biến thể sản phẩm dựa trên size và color
            Optional<SanPhamBienThe> variantOpt = sanPhamBienTheRepository.findById(addToCartDTO.getProductId());
            if (variantOpt.isEmpty()) {
                return false;
            }
            
            SanPhamBienThe variant = variantOpt.get();
            
            // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
            Optional<ChiTietGioHang> existingItem = chiTietGioHangRepository.findByGioHangAndSanPhamBienThe(cart, variant);
            
            if (existingItem.isPresent()) {
                // Cập nhật số lượng
                ChiTietGioHang item = existingItem.get();
                item.setSoLuong(item.getSoLuong() + addToCartDTO.getQuantity());
                item.setThanhTien(item.getGiaBan().multiply(BigDecimal.valueOf(item.getSoLuong())));
                chiTietGioHangRepository.save(item);
            } else {
                // Thêm mới vào giỏ hàng
                ChiTietGioHang newItem = new ChiTietGioHang();
                newItem.setGioHang(cart);
                newItem.setSanPhamBienThe(variant);
                newItem.setSoLuong(addToCartDTO.getQuantity());
                newItem.setGiaBan(variant.getSanPham().getGiaBan());
                newItem.setThanhTien(variant.getSanPham().getGiaBan().multiply(BigDecimal.valueOf(addToCartDTO.getQuantity())));
                chiTietGioHangRepository.save(newItem);
            }
            
            // Cập nhật thời gian giỏ hàng
            cart.setNgayCapNhat(LocalDateTime.now());
            gioHangRepository.save(cart);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateCartItemQuantity(User user, Integer cartItemId, Integer quantity) {
        try {
            Optional<ChiTietGioHang> itemOpt = chiTietGioHangRepository.findById(cartItemId);
            if (itemOpt.isEmpty()) {
                return false;
            }
            
            ChiTietGioHang item = itemOpt.get();
            
            // Kiểm tra xem item có thuộc về user này không
            if (!item.getGioHang().getUser().getId().equals(user.getId())) {
                return false;
            }
            
            item.setSoLuong(quantity);
            item.setThanhTien(item.getGiaBan().multiply(BigDecimal.valueOf(quantity)));
            chiTietGioHangRepository.save(item);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeFromCart(User user, Integer cartItemId) {
        try {
            Optional<ChiTietGioHang> itemOpt = chiTietGioHangRepository.findById(cartItemId);
            if (itemOpt.isEmpty()) {
                return false;
            }
            
            ChiTietGioHang item = itemOpt.get();
            
            // Kiểm tra xem item có thuộc về user này không
            if (!item.getGioHang().getUser().getId().equals(user.getId())) {
                return false;
            }
            
            chiTietGioHangRepository.delete(item);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public GioHang getActiveCart(User user) {
        return gioHangRepository.findByUserAndTrangThai(user, "active").orElse(null);
    }
    
    @Override
    public boolean updateCartStatus(GioHang gioHang) {
        try {
            gioHangRepository.save(gioHang);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Business logic methods for controller
    @Override
    public CartResponseDTO addToCartWithValidation(AddToCartDTO addToCartDTO) {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("User not authenticated");
            }

            // Validate input
            if (addToCartDTO.getProductId() == null || addToCartDTO.getQuantity() == null) {
                throw new ValidationException("Product ID and quantity are required");
            }

            if (addToCartDTO.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than 0");
            }

            // Add to cart
            boolean success = addToCart(currentUser, addToCartDTO);
            if (success) {
                return CartResponseDTO.success("Product added to cart successfully");
            } else {
                return CartResponseDTO.error("Failed to add product to cart");
            }
        } catch (AuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    @Override
    public CartResponseDTO updateCartItemQuantityWithValidation(UpdateCartItemDTO updateCartItemDTO) {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("User not authenticated");
            }

            // Validate input
            if (updateCartItemDTO.getItemId() == null || updateCartItemDTO.getQuantity() == null) {
                throw new ValidationException("Cart item ID and quantity are required");
            }

            if (updateCartItemDTO.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than 0");
            }

            // Update cart item
            boolean success = updateCartItemQuantity(currentUser, updateCartItemDTO.getItemId(), updateCartItemDTO.getQuantity());
            if (success) {
                return CartResponseDTO.success("Cart item updated successfully");
            } else {
                return CartResponseDTO.error("Failed to update cart item");
            }
        } catch (AuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    @Override
    public CartResponseDTO removeFromCartWithValidation(RemoveCartItemDTO removeCartItemDTO) {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("User not authenticated");
            }

            // Validate input
            if (removeCartItemDTO.getItemId() == null) {
                throw new ValidationException("Cart item ID is required");
            }

            // Remove from cart
            boolean success = removeFromCart(currentUser, removeCartItemDTO.getItemId());
            if (success) {
                return CartResponseDTO.success("Item removed from cart successfully");
            } else {
                return CartResponseDTO.error("Failed to remove item from cart");
            }
        } catch (AuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    @Override
    public CartResponseDTO getCartItemCount() {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("User not authenticated");
            }

            // Get active cart
            GioHang activeCart = getActiveCart(currentUser);
            if (activeCart == null) {
                return CartResponseDTO.success("Cart is empty", 0);
            }

            // Count items in cart
            int itemCount = activeCart.getChiTietGioHangs().size();
            return CartResponseDTO.success("Cart item count retrieved successfully", itemCount);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }
}
