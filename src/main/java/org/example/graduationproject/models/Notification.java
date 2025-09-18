package org.example.graduationproject.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "type", columnDefinition = "VARCHAR(50)")
    private String type; // ORDER_CREATED, ORDER_STATUS_CHANGED, etc.

    @Column(name = "is_read", columnDefinition = "BOOLEAN")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "order_id")
    private Integer orderId; // ID của order liên quan

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user; // User nhận thông báo

    @Column(name = "admin_created", columnDefinition = "BOOLEAN")
    private Boolean adminCreated = false; // true nếu admin tạo thông báo cho user

    // Constructor cho thông báo order
    public Notification(String title, String message, String type, User user, Integer orderId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.user = user;
        this.orderId = orderId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.adminCreated = false;
    }

    // Constructor cho thông báo admin
    public Notification(String title, String message, String type, Integer orderId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.orderId = orderId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.adminCreated = true;
    }
}
