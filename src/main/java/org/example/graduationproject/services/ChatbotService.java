package org.example.graduationproject.services;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.output.Response;
import org.example.graduationproject.models.ChatMessage;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatbotService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final GoogleAiGeminiChatModel chatModel;
    private final ProductInfoService productInfoService;
    
    @Autowired
    public ChatbotService(ChatMessageRepository chatMessageRepository,
                         ProductInfoService productInfoService,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.model.name:gemini-1.5-flash}") String modelName,
                         @Value("${gemini.max.tokens:1000}") int maxTokens,
                         @Value("${gemini.temperature:0.7}") double temperature) {
        this.chatMessageRepository = chatMessageRepository;
        this.productInfoService = productInfoService;
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxOutputTokens(maxTokens)
                .temperature(temperature)
                .build();
    }
    
    public String generateResponse(String userMessage, User user) {
        // Xử lý các câu hỏi đặc biệt trước khi gọi AI
        String processedMessage = userMessage.toLowerCase().trim();
        String productInfo = "";
        
        // Kiểm tra nếu khách hỏi về tất cả sản phẩm phẩm
        if (processedMessage.contains("tất cả sản phẩm") || 
            processedMessage.contains("danh sách sản phẩm") ||
            processedMessage.contains("show me all products") ||
            processedMessage.contains("list all products")) {
            productInfo = productInfoService.getAllProductsByCategory();
        }
        // Kiểm tra nếu khách hỏi về danh mục cụ thể
        else if (processedMessage.contains("danh mục") || processedMessage.contains("category")) {
            // Tìm tên danh mục trong câu hỏi
            String categoryName = extractCategoryName(userMessage);
            if (!categoryName.isEmpty()) {
                productInfo = productInfoService.getProductsByCategory(categoryName);
            } else {
                productInfo = productInfoService.getProductCategoriesInfo();
            }
        }
        // Kiểm tra nếu khách hỏi về giá sản phẩm
        else if (isPriceQuery(processedMessage)) {
            productInfo = productInfoService.getProductsByPriceRange(userMessage);
        }
        // Kiểm tra nếu khách hỏi về sản phẩm cụ thể
        else if (processedMessage.contains("sản phẩm") || 
                 processedMessage.contains("product") ||
                 processedMessage.contains("chi tiết") ||
                 processedMessage.contains("details")) {
            // Tìm tên sản phẩm trong câu hỏi
            String productName = extractProductName(userMessage);
            if (!productName.isEmpty()) {
                productInfo = productInfoService.getProductDetails(productName);
            } else {
                productInfo = productInfoService.getProductSummary();
            }
        }
        // Nếu không phải câu hỏi về sản phẩm, chỉ lấy thông tin tổng quan
        else {
            productInfo = productInfoService.getProductSummary();
        }
        
        // Tạo context ngắn gọn để tiết kiệm quota
        StringBuilder context = new StringBuilder();
        context.append("Bạn là trợ lý AI của Shop Reid. Trả lời bằng tiếng Việt thân thiện.\n\n");
        
        // Chỉ thêm thông tin sản phẩm nếu cần thiết và giới hạn độ dài
        if (productInfo != null && !productInfo.trim().isEmpty()) {
            // Giới hạn độ dài thông tin sản phẩm để tiết kiệm quota
            String limitedProductInfo = productInfo.length() > 2000 ? 
                productInfo.substring(0, 2000) + "..." : productInfo;
            context.append("SẢN PHẨM:\n").append(limitedProductInfo).append("\n\n");
        }
        
        context.append("Chính sách: Miễn phí ship trên 100k, đổi trả 7 ngày, Hotline: 0982172169\n\n");
        context.append("Hỏi: ").append(userMessage);
        
        // Gọi AI
        Response<AiMessage> response = chatModel.generate(
                UserMessage.from(context.toString())
        );
        
        return response.content().text();
    }
    
    private String extractCategoryName(String message) {
        // Tìm tên danh mục trong câu hỏi
        String[] categoryKeywords = {
            "áo thun", "quần jean", "giày", "túi", "phụ kiện", "áo sơ mi", "quần short", 
            "váy", "đầm", "áo khoác", "quần tây", "giày thể thao", "túi xách", 
            "mũ", "kính", "đồng hồ", "thắt lưng", "ví", "balo"
        };
        
        for (String keyword : categoryKeywords) {
            if (message.toLowerCase().contains(keyword)) {
                return keyword;
            }
        }
        
        // Nếu không tìm thấy keyword, thử tìm từ sau "danh mục" hoặc "category"
        if (message.toLowerCase().contains("danh mục")) {
            String[] parts = message.split("danh mục");
            if (parts.length > 1) {
                String afterCategory = parts[1].trim();
                String[] words = afterCategory.split("\\s+");
                if (words.length > 0) {
                    return words[0];
                }
            }
        }
        
        if (message.toLowerCase().contains("category")) {
            String[] parts = message.split("category");
            if (parts.length > 1) {
                String afterCategory = parts[1].trim();
                String[] words = afterCategory.split("\\s+");
                if (words.length > 0) {
                    return words[0];
                }
            }
        }
        
        return "";
    }
    
    private boolean isPriceQuery(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Kiểm tra các từ khóa về giá kết hợp với số
        boolean hasPriceKeyword = lowerMessage.contains("giá") || 
                                 lowerMessage.contains("price") ||
                                 lowerMessage.contains("dưới") ||
                                 lowerMessage.contains("từ") ||
                                 lowerMessage.contains("đến") ||
                                 lowerMessage.contains("khoảng") ||
                                 lowerMessage.contains("tầm");
        
        boolean hasNumber = message.matches(".*\\d+.*");
        
        // Chỉ coi là câu hỏi về giá nếu có cả từ khóa và số
        return hasPriceKeyword && hasNumber;
    }
    
    private String extractProductName(String message) {
        // Tìm tên sản phẩm trong câu hỏi
        String[] productKeywords = {
            "áo", "quần", "giày", "túi", "nike", "adidas", "puma", "thun", "jean", "sneaker",
            "áo thun", "quần jean", "giày thể thao", "túi xách", "áo sơ mi", "quần short",
            "váy", "đầm", "áo khoác", "quần tây", "mũ", "kính", "đồng hồ", "thắt lưng", "ví", "balo",
            "converse", "vans", "new balance", "reebok", "under armour", "champion"
        };
        
        for (String keyword : productKeywords) {
            if (message.toLowerCase().contains(keyword)) {
                return keyword;
            }
        }
        
        // Nếu không tìm thấy keyword, thử tìm từ sau "sản phẩm" hoặc "product"
        if (message.toLowerCase().contains("sản phẩm")) {
            String[] parts = message.split("sản phẩm");
            if (parts.length > 1) {
                String afterProduct = parts[1].trim();
                String[] words = afterProduct.split("\\s+");
                if (words.length > 0) {
                    return words[0];
                }
            }
        }
        
        if (message.toLowerCase().contains("product")) {
            String[] parts = message.split("product");
            if (parts.length > 1) {
                String afterProduct = parts[1].trim();
                String[] words = afterProduct.split("\\s+");
                if (words.length > 0) {
                    return words[0];
                }
            }
        }
        
        return "";
    }
    
    public ChatMessage saveMessage(User user, String message, String response) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setResponse(response);
        chatMessage.setMessageType(ChatMessage.MessageType.USER);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setIsRead(false);
        
        return chatMessageRepository.save(chatMessage);
    }
    
    public ChatMessage saveBotResponse(User user, String message, String response) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setResponse(response);
        chatMessage.setMessageType(ChatMessage.MessageType.BOT);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setIsRead(false);
        
        return chatMessageRepository.save(chatMessage);
    }
    
    public List<ChatMessage> getChatHistory(User user) {
        return chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public void markAsRead(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        chatMessageRepository.save(message);
    }
    
    public long getUnreadCount(User user) {
        return chatMessageRepository.countByUserAndIsReadFalse(user);
    }
}
