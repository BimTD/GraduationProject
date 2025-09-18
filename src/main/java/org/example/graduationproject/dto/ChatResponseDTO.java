package org.example.graduationproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private Long id;
    private String message;
    private String response;
    private String messageType;
    private LocalDateTime createdAt;
    private Boolean isRead;
}




















