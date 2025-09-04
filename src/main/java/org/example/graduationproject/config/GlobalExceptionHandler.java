package org.example.graduationproject.config;

import org.example.graduationproject.dto.CartResponseDTO;
import org.example.graduationproject.dto.OrderResponseDTO;
import org.example.graduationproject.dto.ProductResponseDTO;
import org.example.graduationproject.dto.HomeResponseDTO;
import org.example.graduationproject.dto.CartPageResponseDTO;
import org.example.graduationproject.dto.CheckoutResponseDTO;
import org.example.graduationproject.exceptions.AuthenticationException;
import org.example.graduationproject.exceptions.CartException;
import org.example.graduationproject.exceptions.ResourceNotFoundException;
import org.example.graduationproject.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String path = request.getDescription(false);
        
        // Determine response type based on request path
        if (path.contains("/api/cart")) {
            CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (path.contains("/orders") || path.contains("/api/orders")) {
            OrderResponseDTO response = OrderResponseDTO.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (path.contains("/api/products")) {
            ProductResponseDTO response = new ProductResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (path.contains("/home")) {
            HomeResponseDTO response = new HomeResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (path.contains("/cart")) {
            CartPageResponseDTO response = new CartPageResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (path.contains("/checkout")) {
            CheckoutResponseDTO response = new CheckoutResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            // Default to CartResponseDTO for backward compatibility
            CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex, WebRequest request) {
        String path = request.getDescription(false);
        
        // Determine response type based on request path
        if (path.contains("/api/cart")) {
            CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } else if (path.contains("/orders") || path.contains("/api/orders")) {
            OrderResponseDTO response = OrderResponseDTO.error(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } else if (path.contains("/api/products")) {
            ProductResponseDTO response = new ProductResponseDTO(false, ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } else if (path.contains("/home")) {
            HomeResponseDTO response = new HomeResponseDTO(false, ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } else if (path.contains("/cart")) {
            CartPageResponseDTO response = new CartPageResponseDTO(false, ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } else if (path.contains("/checkout")) {
            CheckoutResponseDTO response = new CheckoutResponseDTO(false, ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } else {
            // Default to CartResponseDTO for backward compatibility
            CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String path = request.getDescription(false);
        
        // Determine response type based on request path
        if (path.contains("/api/cart")) {
            CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (path.contains("/orders") || path.contains("/api/orders")) {
            OrderResponseDTO response = OrderResponseDTO.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (path.contains("/api/products")) {
            ProductResponseDTO response = new ProductResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (path.contains("/home")) {
            HomeResponseDTO response = new HomeResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (path.contains("/cart")) {
            CartPageResponseDTO response = new CartPageResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (path.contains("/checkout")) {
            CheckoutResponseDTO response = new CheckoutResponseDTO(false, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            // Default to CartResponseDTO for backward compatibility
            CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<CartResponseDTO> handleCartException(CartException ex, WebRequest request) {
        CartResponseDTO response = CartResponseDTO.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
        // Log the full exception for debugging
        ex.printStackTrace();
        
        String path = request.getDescription(false);
        
        // Determine response type based on request path
        if (path.contains("/api/cart")) {
            CartResponseDTO response = CartResponseDTO.error("Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (path.contains("/orders") || path.contains("/api/orders")) {
            OrderResponseDTO response = OrderResponseDTO.error("Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (path.contains("/api/products")) {
            ProductResponseDTO response = new ProductResponseDTO(false, "Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (path.contains("/home")) {
            HomeResponseDTO response = new HomeResponseDTO(false, "Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (path.contains("/cart")) {
            CartPageResponseDTO response = new CartPageResponseDTO(false, "Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (path.contains("/checkout")) {
            CheckoutResponseDTO response = new CheckoutResponseDTO(false, "Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            // Default to CartResponseDTO for backward compatibility
            CartResponseDTO response = CartResponseDTO.error("Lỗi hệ thống");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Method để tạo error response với thông tin chi tiết hơn (cho debugging)
    private Map<String, Object> createErrorResponse(String message, String path, LocalDateTime timestamp) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("timestamp", timestamp);
        return errorResponse;
    }
}
