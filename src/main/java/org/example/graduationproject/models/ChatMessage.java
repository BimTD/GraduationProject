package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "message", columnDefinition = "NVARCHAR(MAX)")
    private String message;
    
    @Column(name = "response", columnDefinition = "NVARCHAR(MAX)")
    private String response;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    public enum MessageType {
        USER, BOT
    }
}

