package org.example.graduationproject.controllers;

import org.example.graduationproject.models.Notification;
import org.example.graduationproject.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
public class WebSocketController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notifications/mark-read")
    @SendTo("/queue/notifications")
    public void markNotificationAsRead(Long notificationId, Principal principal) {
        if (principal != null) {
            notificationService.markAsRead(notificationId);
        }
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public List<Notification> getUserNotifications(Principal principal) {
        if (principal != null) {
            // Lấy user từ principal (cần implement logic này)
            // User user = userService.findByUsername(principal.getName());
            // return notificationService.getUserNotifications(user);
        }
        return List.of();
    }
}
