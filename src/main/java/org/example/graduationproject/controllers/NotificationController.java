package org.example.graduationproject.controllers;

import org.example.graduationproject.models.Notification;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.NotificationService;
import org.example.graduationproject.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok("Marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking notification as read");
        }
    }


    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount() {
        try {
            User user = authenticationService.getCurrentUser();
            if (user != null) {
                long count = notificationService.getUnreadCount(user);
                return ResponseEntity.ok(count);
            }
            return ResponseEntity.ok(0L);
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }

    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<List<Notification>> getRecentNotifications() {
        try {
            User user = authenticationService.getCurrentUser();
            if (user != null) {
                List<Notification> notifications = notificationService.getUserNotifications(user);
                // Chỉ lấy 5 thông báo gần nhất
                List<Notification> recentNotifications = notifications.stream()
                        .limit(5)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(recentNotifications);
            }
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/api/admin/unread-count")
    @ResponseBody
    public ResponseEntity<Long> getAdminUnreadCount() {
        try {
            long count = notificationService.getAdminUnreadCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }

    @GetMapping("/api/admin/recent")
    @ResponseBody
    public ResponseEntity<List<Notification>> getAdminRecentNotifications() {
        try {
            List<Notification> notifications = notificationService.getAdminNotifications();
            // Chỉ lấy 5 thông báo gần nhất
            List<Notification> recentNotifications = notifications.stream()
                    .limit(5)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(recentNotifications);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}
