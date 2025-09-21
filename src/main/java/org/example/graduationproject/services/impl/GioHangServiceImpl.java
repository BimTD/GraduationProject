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
    
    /**
     * Tính giá cuối cùng của sản phẩm sau khi áp dụng promotion (theo phần trăm)
     */
    private BigDecimal calculateFinalPrice(SanPhamBienThe variant) {
        BigDecimal giaBan = variant.getSanPham().getGiaBan();
        BigDecimal khuyenMai = variant.getSanPham().getKhuyenMai() != null ? 
            variant.getSanPham().getKhuyenMai() : BigDecimal.ZERO;
        
        // Tính giá sau khi giảm theo phần trăm
        // Giá cuối = giaBan * (100 - khuyenMai) / 100
        BigDecimal phanTramGiam = BigDecimal.valueOf(100).subtract(khuyenMai);
        return giaBan.multiply(phanTramGiam).divide(BigDecimal.valueOf(100));
    }

    @Override
    public GioHang getOrCreateActiveCart(User user) {
        // Tìm giỏ hàng active hoặc đã gửi email (người dùng vẫn có thể thanh toán)
        Optional<GioHang> existingCart = gioHangRepository.findByUserAndActiveOrEmailSent(user);
        
        if (existingCart.isPresent()) {
            GioHang cart = existingCart.get();
            
            // Kiểm tra xem giỏ hàng có hết hạn thanh toán không
            if (isCartPaymentExpired(cart)) {
                // Nếu hết hạn, chuyển giỏ hàng cũ sang abandoned và tạo giỏ hàng mới
                cart.setTrangThai("abandoned");
                cart.setNgayCapNhat(LocalDateTime.now());
                gioHangRepository.save(cart);
                System.out.println("Đã chuyển giỏ hàng hết hạn sang abandoned cho user: " + user.getEmail());
                
                // Tạo giỏ hàng mới
                GioHang newCart = new GioHang();
                newCart.setUser(user);
                newCart.setTrangThai("active");
                newCart.setEmailSent(false);
                newCart.setNgayTao(LocalDateTime.now());
                newCart.setNgayCapNhat(LocalDateTime.now());
                
                return gioHangRepository.save(newCart);
            }
            
            return cart;
        }

        // Tạo giỏ hàng mới
        GioHang newCart = new GioHang();
        newCart.setUser(user);
        newCart.setTrangThai("active");
        newCart.setEmailSent(false);
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
            
            // Tính giá cuối cùng với promotion
            BigDecimal giaCuoiCung = calculateFinalPrice(variant);
            
            if (existingItem.isPresent()) {
                // Cập nhật số lượng
                ChiTietGioHang item = existingItem.get();
                item.setSoLuong(item.getSoLuong() + addToCartDTO.getQuantity());
                item.setGiaBan(giaCuoiCung); // Cập nhật giá đã áp dụng promotion
                item.setThanhTien(giaCuoiCung.multiply(BigDecimal.valueOf(item.getSoLuong())));
                chiTietGioHangRepository.save(item);
            } else {
                // Thêm mới vào giỏ hàng
                ChiTietGioHang newItem = new ChiTietGioHang();
                newItem.setGioHang(cart);
                newItem.setSanPhamBienThe(variant);
                newItem.setSoLuong(addToCartDTO.getQuantity());
                newItem.setGiaBan(giaCuoiCung); // Sử dụng giá đã áp dụng promotion
                newItem.setThanhTien(giaCuoiCung.multiply(BigDecimal.valueOf(addToCartDTO.getQuantity())));
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
            
            // Tính giá cuối cùng với promotion
            BigDecimal giaCuoiCung = calculateFinalPrice(item.getSanPhamBienThe());
            
            item.setSoLuong(quantity);
            item.setGiaBan(giaCuoiCung); // Cập nhật giá đã áp dụng promotion
            item.setThanhTien(giaCuoiCung.multiply(BigDecimal.valueOf(quantity)));
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
        // Tìm giỏ hàng active hoặc đã gửi email (người dùng vẫn có thể thanh toán)
        return gioHangRepository.findByUserAndActiveOrEmailSent(user).orElse(null);
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
                throw new AuthenticationException("Người dùng chưa được xác thực");
            }

            // Validate input
            if (addToCartDTO.getProductId() == null || addToCartDTO.getQuantity() == null) {
                throw new ValidationException("Mã sản phẩm và số lượng là bắt buộc");
            }

            if (addToCartDTO.getQuantity() <= 0) {
                throw new ValidationException("Số lượng phải lớn hơn 0");
            }

            // Add to cart
            boolean success = addToCart(currentUser, addToCartDTO);
            if (success) {
                return CartResponseDTO.success("Sản phẩm đã được thêm vào giỏ hàng thành công");
            } else {
                return CartResponseDTO.error("Không thể thêm sản phẩm vào giỏ hàng");
            }
        } catch (AuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đã xảy ra lỗi không mong muốn", e);
        }
    }

    @Override
    public CartResponseDTO updateCartItemQuantityWithValidation(UpdateCartItemDTO updateCartItemDTO) {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("Người dùng chưa được xác thực");
            }

            // Validate input
            if (updateCartItemDTO.getItemId() == null || updateCartItemDTO.getQuantity() == null) {
                throw new ValidationException("Cần phải nhập ID và số lượng mặt hàng trong giỏ hàng");
            }

            if (updateCartItemDTO.getQuantity() <= 0) {
                throw new ValidationException("Số lượng phải lớn hơn 0");
            }

            // Update cart item
            boolean success = updateCartItemQuantity(currentUser, updateCartItemDTO.getItemId(), updateCartItemDTO.getQuantity());
            if (success) {
                return CartResponseDTO.success("Đã cập nhật thành công mục giỏ hàng");
            } else {
                return CartResponseDTO.error("Không cập nhật được sản phẩm trong giỏ hàng");
            }
        } catch (AuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đã xảy ra lỗi không mong muốn", e);
        }
    }

    @Override
    public CartResponseDTO removeFromCartWithValidation(RemoveCartItemDTO removeCartItemDTO) {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("Người dùng chưa được xác thực");
            }

            // Validate input
            if (removeCartItemDTO.getItemId() == null) {
                throw new ValidationException("Mã sản phẩm giỏ hàng là bắt buộc");
            }

            // Remove from cart
            boolean success = removeFromCart(currentUser, removeCartItemDTO.getItemId());
            if (success) {
                return CartResponseDTO.success("Đã xóa sản phẩm khỏi giỏ hàng thành công");
            } else {
                return CartResponseDTO.error("Không thể xóa sản phẩm khỏi giỏ hàng");
            }
        } catch (AuthenticationException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đã xảy ra lỗi không mong muốn", e);
        }
    }

    @Override
    public CartResponseDTO getCartItemCount() {
        try {
            // Validate authentication
            User currentUser = authenticationService.getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("Người dùng chưa được xác thực");
            }

            // Get active cart
            GioHang activeCart = getActiveCart(currentUser);
            if (activeCart == null) {
                return CartResponseDTO.success("Giỏ hàng trống", 0);
            }

            // Count items in cart
            int itemCount = activeCart.getChiTietGioHangs().size();
            return CartResponseDTO.success("Đã lấy lại số lượng mặt hàng trong giỏ hàng thành công", itemCount);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đã xảy ra lỗi không mong muốn", e);
        }
    }

    @Override
    public boolean isCartPaymentExpired(GioHang cart) {
        if (cart == null || !cart.getEmailSent() || cart.getEmailSentAt() == null) {
            return false; // Chưa gửi email thì không hết hạn
        }
        
        // Kiểm tra xem đã quá 2 phút kể từ khi gửi email chưa
        LocalDateTime expirationTime = cart.getEmailSentAt().plusMinutes(2);
        return LocalDateTime.now().isAfter(expirationTime);
    }

    @Override
    public boolean canUserCheckout(User user) {
        GioHang cart = getActiveCart(user);
        if (cart == null) {
            return false; // Không có giỏ hàng
        }
        
        // Nếu chưa gửi email thì có thể thanh toán
        if (!cart.getEmailSent()) {
            return true;
        }
        
        // Nếu đã gửi email thì kiểm tra thời gian hết hạn
        return !isCartPaymentExpired(cart);
    }
}
