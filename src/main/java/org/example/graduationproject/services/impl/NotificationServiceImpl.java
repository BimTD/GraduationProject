package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.Notification;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.NotificationRepository;
import org.example.graduationproject.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public Notification createNotification(String title, String message, String type, User user, Integer orderId) {
        Notification notification = new Notification(title, message, type, user, orderId);
        Notification savedNotification = notificationRepository.save(notification);
        
        // Gửi thông báo real-time
        sendRealTimeNotification(savedNotification);
        
        return savedNotification;
    }

    @Override
    public Notification createAdminNotification(String title, String message, String type, Integer orderId) {
        Notification notification = new Notification(title, message, type, orderId);
        Notification savedNotification = notificationRepository.save(notification);
        
        // Gửi thông báo real-time cho admin
        sendNotificationToAllAdmins(title, message, type, orderId);
        
        return savedNotification;
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getUnreadUserNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public List<Notification> getAdminNotifications() {
        return notificationRepository.findAdminNotifications();
    }

    @Override
    public long getAdminUnreadCount() {
        return notificationRepository.countAdminUnreadNotifications();
    }

    @Override
    public void sendRealTimeNotification(Notification notification) {
        if (notification.getUser() != null) {
            // Gửi cho user cụ thể
            messagingTemplate.convertAndSendToUser(
                notification.getUser().getUsername(),
                "/queue/notifications",
                notification
            );
        } else {
            // Gửi cho tất cả admin
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
        }
    }

    @Override
    public void sendNotificationToAllAdmins(String title, String message, String type, Integer orderId) {
        Notification notification = new Notification(title, message, type, orderId);
        messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
    }

    @Override
    public void sendNotificationToUser(User user, String title, String message, String type, Integer orderId) {
        Notification notification = new Notification(title, message, type, user, orderId);
        messagingTemplate.convertAndSendToUser(
            user.getUsername(),
            "/queue/notifications",
            notification
        );
        System.out.println("Sending notification to user: " + user.getUsername() + " - " + title);
    }
}
