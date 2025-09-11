package org.example.graduationproject.repositories;

import org.example.graduationproject.models.Notification;
import org.example.graduationproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy thông báo của user, sắp xếp theo thời gian tạo mới nhất
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Lấy thông báo chưa đọc của user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Đếm số thông báo chưa đọc của user
    long countByUserAndIsReadFalse(User user);
    
    // Lấy thông báo admin (không có user cụ thể)
    @Query("SELECT n FROM Notification n WHERE n.adminCreated = true ORDER BY n.createdAt DESC")
    List<Notification> findAdminNotifications();
    
    // Đếm số thông báo chưa đọc của admin
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.adminCreated = true AND n.isRead = false")
    long countAdminUnreadNotifications();
    
    // Lấy thông báo theo order ID
    List<Notification> findByOrderIdOrderByCreatedAtDesc(Integer orderId);
    
    // Lấy thông báo theo type
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
}
