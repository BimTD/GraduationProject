package org.example.graduationproject.controllers.user;

import org.example.graduationproject.dto.AddToCartDTO;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.UserRepository;
import org.example.graduationproject.services.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartDTO addToCartDTO) {
        try {
            // Lấy thông tin user hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Không tìm thấy thông tin người dùng"));
            }

            // Thêm vào giỏ hàng
            boolean success = gioHangService.addToCart(user, addToCartDTO);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm sản phẩm vào giỏ hàng"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể thêm sản phẩm vào giỏ hàng"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            var cart = gioHangService.getActiveCart(user);
            int count = 0;
            if (cart != null && cart.getChiTietGioHangs() != null) {
                count = cart.getChiTietGioHangs().size();
            }

            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    @PostMapping("/update-quantity")
    public ResponseEntity<?> updateCartItemQuantity(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Không tìm thấy thông tin người dùng"));
            }

            Object itemIdObj = request.get("itemId");
            Object quantityObj = request.get("quantity");
            
            Integer itemId = null;
            Integer quantity = null;
            
            // Xử lý itemId
            if (itemIdObj instanceof Integer) {
                itemId = (Integer) itemIdObj;
            } else if (itemIdObj instanceof String) {
                try {
                    itemId = Integer.parseInt((String) itemIdObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
                }
            }
            
            // Xử lý quantity
            if (quantityObj instanceof Integer) {
                quantity = (Integer) quantityObj;
            } else if (quantityObj instanceof String) {
                try {
                    quantity = Integer.parseInt((String) quantityObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số lượng không hợp lệ"));
                }
            }

            if (itemId == null || quantity == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin"));
            }
            
            // Kiểm tra giá trị hợp lệ
            if (itemId <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }
            
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số lượng phải lớn hơn 0"));
            }

            boolean success = gioHangService.updateCartItemQuantity(user, itemId, quantity);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật số lượng"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể cập nhật số lượng"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Không tìm thấy thông tin người dùng"));
            }

            Object itemIdObj = request.get("itemId");
            
            Integer itemId = null;
            
            // Xử lý itemId
            if (itemIdObj instanceof Integer) {
                itemId = (Integer) itemIdObj;
            } else if (itemIdObj instanceof String) {
                try {
                    itemId = Integer.parseInt((String) itemIdObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
                }
            }

            if (itemId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin"));
            }
            
            // Kiểm tra giá trị hợp lệ
            if (itemId <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }

            boolean success = gioHangService.removeFromCart(user, itemId);
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa sản phẩm khỏi giỏ hàng"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể xóa sản phẩm"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi hệ thống"));
        }
    }
}
