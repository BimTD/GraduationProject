package org.example.graduationproject.services;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.OrderResponseDTO;
import org.example.graduationproject.dto.CancelOrderDTO;
import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;

import java.util.List;

public interface HoaDonService {
    
    // Tạo hóa đơn từ giỏ hàng
    HoaDon createOrderFromCart(User user, CheckoutDTO checkoutDTO);
    
    // Lấy tất cả hóa đơn của user
    List<HoaDon> getUserOrders(User user);
    
    // Lấy hóa đơn theo ID
    HoaDon getOrderById(Integer orderId);
    
    // Lấy hóa đơn của user theo ID
    HoaDon getUserOrderById(User user, Integer orderId);
    
    // Cập nhật trạng thái hóa đơn
    boolean updateOrderStatus(Integer orderId, String newStatus);
    
    boolean updateOrderStatusAndRestoreStock(Integer orderId, String newStatus);
    
    // Lưu hóa đơn
    HoaDon saveOrder(HoaDon hoaDon);
    
    // Hủy hóa đơn
    boolean cancelOrder(User user, Integer orderId);
    
    // Business logic methods for controller
    OrderResponseDTO getUserOrdersWithValidation();
    OrderResponseDTO getUserOrderDetailWithValidation(Integer orderId);
    OrderResponseDTO cancelOrderWithValidation(CancelOrderDTO cancelOrderDTO);
}
