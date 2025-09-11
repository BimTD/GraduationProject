package org.example.graduationproject.services;

import org.example.graduationproject.models.Notification;
import org.example.graduationproject.models.User;

import java.util.List;

public interface NotificationService {
    
    // Tạo thông báo mới
    Notification createNotification(String title, String message, String type, User user, Integer orderId);
    
    // Tạo thông báo cho admin
    Notification createAdminNotification(String title, String message, String type, Integer orderId);
    
    // Lấy thông báo của user
    List<Notification> getUserNotifications(User user);
    
    // Lấy thông báo chưa đọc của user
    List<Notification> getUnreadUserNotifications(User user);
    
    // Đánh dấu thông báo đã đọc
    void markAsRead(Long notificationId);
    
    // Đánh dấu tất cả thông báo của user đã đọc
    void markAllAsRead(User user);
    
    // Đếm số thông báo chưa đọc
    long getUnreadCount(User user);
    
    // Lấy thông báo admin
    List<Notification> getAdminNotifications();
    
    // Đếm số thông báo chưa đọc của admin
    long getAdminUnreadCount();
    
    // Gửi thông báo real-time qua WebSocket
    void sendRealTimeNotification(Notification notification);
    
    // Gửi thông báo cho tất cả admin
    void sendNotificationToAllAdmins(String title, String message, String type, Integer orderId);
    
    // Gửi thông báo cho user cụ thể
    void sendNotificationToUser(User user, String title, String message, String type, Integer orderId);
}
