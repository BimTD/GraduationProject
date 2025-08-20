package org.example.graduationproject.services;

import org.example.graduationproject.dto.CheckoutDTO;
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
    
    // Hủy hóa đơn
    boolean cancelOrder(User user, Integer orderId);
}
