package org.example.graduationproject.controllers;

import org.example.graduationproject.dto.ChatRequestDTO;
import org.example.graduationproject.dto.ChatResponseDTO;
import org.example.graduationproject.models.ChatMessage;
import org.example.graduationproject.models.User;
import org.example.graduationproject.services.ChatbotService;
import org.example.graduationproject.services.ProductInfoService;
import org.example.graduationproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatController {
    
    @Autowired
    private ChatbotService chatbotService;
    
    @Autowired
    private ProductInfoService productInfoService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/chat")
    public ChatResponseDTO sendMessage(ChatRequestDTO request, Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            
            // Tạo phản hồi từ AI
            String aiResponse = chatbotService.generateResponse(request.getMessage(), user);
            
            // Lưu tin nhắn người dùng
            ChatMessage userMessage = chatbotService.saveMessage(user, request.getMessage(), null);
            
            // Lưu phản hồi AI
            ChatMessage botMessage = chatbotService.saveBotResponse(user, request.getMessage(), aiResponse);
            
            // Tạo response
            ChatResponseDTO response = new ChatResponseDTO();
            response.setId(botMessage.getId());
            response.setMessage(request.getMessage());
            response.setResponse(aiResponse);
            response.setMessageType("BOT");
            response.setCreatedAt(botMessage.getCreatedAt());
            response.setIsRead(false);
            
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatResponseDTO> getChatHistory(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return chatbotService.getChatHistory(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @PostMapping("/api/chat/mark-read")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@RequestParam Long messageId, Principal principal) {
        try {
            chatbotService.markAsRead(messageId);
            return ResponseEntity.ok("Tin nhắn được đánh dấu là đã đọc");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đánh dấu tin nhắn là đã đọc");
        }
    }
    
    @GetMapping("/api/chat/products/all")
    @ResponseBody
    public ResponseEntity<String> getAllProducts(Principal principal) {
        try {
            String products = productInfoService.getAllProductsByCategory();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi nhận sản phẩm: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/chat/products/search")
    @ResponseBody
    public ResponseEntity<String> searchProduct(@RequestParam String productName, Principal principal) {
        try {
            String product = productInfoService.getProductDetails(productName);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/chat/products/category")
    @ResponseBody
    public ResponseEntity<String> getProductsByCategory(@RequestParam String categoryName, Principal principal) {
        try {
            String products = productInfoService.getProductsByCategory(categoryName);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy sản phẩm theo danh mục: " + e.getMessage());
        }
    }
    
    private ChatResponseDTO convertToDTO(ChatMessage message) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setId(message.getId());
        dto.setMessage(message.getMessage());
        dto.setResponse(message.getResponse());
        dto.setMessageType(message.getMessageType().toString());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIsRead(message.getIsRead());
        return dto;
    }
}
