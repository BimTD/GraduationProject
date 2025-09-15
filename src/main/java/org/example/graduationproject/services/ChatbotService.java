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
        // Lấy lịch sử chat gần đây
        List<ChatMessage> recentMessages = chatMessageRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .limit(5)
                .toList();
        
        // Xử lý các câu hỏi đặc biệt trước khi gọi AI
        String processedMessage = userMessage.toLowerCase().trim();
        String productInfo = "";
        
        // Kiểm tra nếu khách hỏi về tất cả sản phẩm
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
        
        // Tạo context cho AI với thông tin sản phẩm thực tế
        StringBuilder context = new StringBuilder();
        context.append("Bạn là một trợ lý AI thông minh cho cửa hàng thời trang 'Shop Reid'. ");
        context.append("Hãy trả lời các câu hỏi về sản phẩm, đơn hàng, chính sách và hỗ trợ khách hàng. ");
        context.append("Hãy trả lời bằng tiếng Việt một cách thân thiện, nhiệt tình và hữu ích.\n\n");
        
        // Thêm thông tin sản phẩm thực tế từ database
        context.append("THÔNG TIN SẢN PHẨM HIỆN TẠI:\n");
        context.append(productInfo);
        context.append("\n");
        
        context.append("THÔNG TIN CỬA HÀNG:\n");
        context.append("- Miễn phí vận chuyển cho đơn hàng trên 100,000 VNĐ\n");
        context.append("- Hỗ trợ đổi trả trong 7 ngày\n");
        context.append("- Giao hàng toàn quốc\n");
        context.append("- Hotline: 0982172169\n");
        context.append("- Website: /shop để xem tất cả sản phẩm\n\n");
        
        // Thêm lịch sử chat
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = recentMessages.get(i);
            if (msg.getMessageType() == ChatMessage.MessageType.USER) {
                context.append("Người dùng: ").append(msg.getMessage()).append("\n");
            } else {
                context.append("AI: ").append(msg.getResponse()).append("\n");
            }
        }
        
        context.append("Người dùng: ").append(userMessage);
        
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
