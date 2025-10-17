package org.example.graduationproject.services;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.output.Response;
import org.example.graduationproject.models.ChatMessage;
import org.example.graduationproject.models.User;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.repositories.ChatMessageRepository;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

// Enum để phân loại câu hỏi
enum QuestionType {
    ALL_PRODUCTS,           // Tất cả sản phẩm
    ALL_CATEGORIES,         // Tất cả danh mục
    CATEGORY_SPECIFIC,      // Danh mục cụ thể
    PRICE_QUERY,           // Câu hỏi về giá
    PRODUCT_SEARCH,        // Tìm kiếm sản phẩm
    NEW_PRODUCTS,          // Sản phẩm mới
    DISCOUNTED_PRODUCTS,   // Sản phẩm khuyến mại
    STORE_OVERVIEW,        // Tổng quan cửa hàng
    PRODUCT_DETAILS,       // Chi tiết sản phẩm
    ORDER_QUERY,           // Câu hỏi về đơn hàng
    ORDER_STATUS,          // Trạng thái đơn hàng
    ORDER_HISTORY,         // Lịch sử đơn hàng
    ORDER_CANCEL,          // Hủy đơn hàng
    ORDER_TRACKING,        // Theo dõi đơn hàng
    CART_QUERY,            // Câu hỏi về giỏ hàng
    PAYMENT_QUERY,         // Câu hỏi về thanh toán
    SHIPPING_QUERY,        // Câu hỏi về giao hàng
    GENERAL_QUESTION       // Câu hỏi chung
}

@Service
public class ChatbotService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final GoogleAiGeminiChatModel chatModel;
    private final ProductInfoService productInfoService;
    private final HoaDonService hoaDonService;
    
    @Autowired
    public ChatbotService(ChatMessageRepository chatMessageRepository,
                         ProductInfoService productInfoService,
                         HoaDonService hoaDonService,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.model.name:gemini-2.5-flash}") String modelName,
                         @Value("${gemini.max.tokens:1000}") int maxTokens,
                         @Value("${gemini.temperature:0.7}") double temperature) {
        this.chatMessageRepository = chatMessageRepository;
        this.productInfoService = productInfoService;
        this.hoaDonService = hoaDonService;
        
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxOutputTokens(maxTokens)
                .temperature(temperature)
                .build();
    }
    
    public String generateResponse(String userMessage, User user) {
        try {
            // Xử lý các câu hỏi đặc biệt trước khi gọi AI
            String processedMessage = userMessage.toLowerCase().trim();
            String productInfo = "";
            
            // Phân loại câu hỏi thông minh
            QuestionType questionType = classifyQuestion(userMessage);
            
            switch (questionType) {
                case ALL_PRODUCTS:
                    productInfo = productInfoService.getAllProductsByCategory();
                    break;
                    
                case ALL_CATEGORIES:
                    productInfo = productInfoService.getProductCategoriesInfo();
                    break;
                    
                case CATEGORY_SPECIFIC:
                    String categoryName = extractCategoryName(userMessage);
                    if (!categoryName.isEmpty()) {
                        productInfo = productInfoService.getProductsByCategory(categoryName);
                    } else {
                        productInfo = productInfoService.getProductCategoriesInfo();
                    }
                    break;
                    
                case PRICE_QUERY:
                    productInfo = productInfoService.getProductsByPriceRangeSmart(userMessage);
                    break;
                    
                case PRODUCT_SEARCH:
                    productInfo = productInfoService.searchProductsIntelligently(userMessage);
                    break;
                    
                case NEW_PRODUCTS:
                    productInfo = productInfoService.getNewestProducts(6);
                    break;
                    
                case DISCOUNTED_PRODUCTS:
                    productInfo = productInfoService.getDiscountedProducts();
                    break;
                    
                case STORE_OVERVIEW:
                    productInfo = productInfoService.getStoreOverview();
                    break;
                    
                case PRODUCT_DETAILS:
                    String productName = extractProductName(userMessage);
                    if (!productName.isEmpty()) {
                        productInfo = productInfoService.getProductDetails(productName);
                    } else {
                        productInfo = productInfoService.getProductSummary();
                    }
                    break;
                    
                case ORDER_QUERY:
                    productInfo = getOrderInfo(user);
                    break;
                    
                case ORDER_STATUS:
                    productInfo = getOrderStatusInfo(user, userMessage);
                    break;
                    
                case ORDER_HISTORY:
                    productInfo = getOrderHistoryInfo(user);
                    break;
                    
                case ORDER_CANCEL:
                    productInfo = getOrderCancelInfo(user, userMessage);
                    break;
                    
                case ORDER_TRACKING:
                    productInfo = getOrderTrackingInfo(user, userMessage);
                    break;
                    
                case CART_QUERY:
                    productInfo = getCartInfo(user);
                    break;
                    
                case PAYMENT_QUERY:
                    productInfo = getPaymentInfo(userMessage);
                    break;
                    
                case SHIPPING_QUERY:
                    productInfo = getShippingInfo(userMessage);
                    break;
                    
                case GENERAL_QUESTION:
                default:
                    productInfo = productInfoService.getProductSummary();
                    break;
            }
            
            // Tạo context thông minh dựa trên loại câu hỏi
            StringBuilder context = new StringBuilder();
            context.append("Bạn là trợ lý AI thông minh của Shop Reid. Trả lời bằng tiếng Việt thân thiện và chuyên nghiệp.\n\n");
            
            // Thêm thông tin sản phẩm phù hợp
            if (productInfo != null && !productInfo.trim().isEmpty()) {
                // Giới hạn độ dài thông tin sản phẩm để tiết kiệm quota
                String limitedProductInfo = productInfo.length() > 2000 ?
                    productInfo.substring(0, 2000) + "..." : productInfo;
                context.append("THÔNG TIN SẢN PHẨM:\n").append(limitedProductInfo).append("\n\n");
            }
            
            // Thêm thông tin cửa hàng
            context.append("THÔNG TIN CỬA HÀNG:\n");
            context.append("• Tên: Shop Reid\n");
            context.append("• Chính sách: Miễn phí ship trên 100k, đổi trả 7 ngày\n");
            context.append("• Hotline: 0982172169\n");
            context.append("• Địa chỉ: [Địa chỉ cửa hàng]\n\n");
            
            // Thêm hướng dẫn tìm kiếm
            context.append("HƯỚNG DẪN TÌM KIẾM:\n");
            context.append("• Tìm sản phẩm: 'áo thun nike', 'giày adidas'\n");
            context.append("• Tìm theo giá: 'sản phẩm dưới 500k', 'từ 200k đến 500k'\n");
            context.append("• Tìm theo màu: 'áo màu đỏ', 'giày màu trắng'\n");
            context.append("• Tìm theo size: 'áo size M', 'giày size 42'\n");
            context.append("• Tìm theo giới tính: 'áo nam', 'váy nữ'\n");
            context.append("• Xem sản phẩm mới: 'sản phẩm mới nhất'\n");
            context.append("• Xem khuyến mại: 'sản phẩm khuyến mại'\n\n");
            
            context.append("CÂU HỎI CỦA KHÁCH HÀNG: ").append(userMessage);
            
            // Gọi AI với retry mechanism
            Response<AiMessage> response = callGeminiWithRetry(context.toString(), 3);
            
            return response.content().text();
            
        } catch (Exception e) {
            // Log lỗi chi tiết
            System.err.println("Lỗi khi gọi Gemini API: " + e.getMessage());
            e.printStackTrace();
            
            // Trả về phản hồi mặc định khi có lỗi
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ hotline: 0982172169";
        }
    }
    
    private Response<AiMessage> callGeminiWithRetry(String context, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Thử gọi Gemini API lần " + attempt + "/" + maxRetries);
                Response<AiMessage> response = chatModel.generate(UserMessage.from(context));
                System.out.println("Gọi Gemini API thành công lần " + attempt);
                return response;
            } catch (Exception e) {
                lastException = e;
                System.err.println("Lần thử " + attempt + " thất bại: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // Đợi 2 giây trước khi thử lại
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread bị interrupt", ie);
                    }
                }
            }
        }
        
        // Nếu tất cả lần thử đều thất bại
        throw new RuntimeException("Không thể gọi Gemini API sau " + maxRetries + " lần thử", lastException);
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
    
    /**
     * Phân loại câu hỏi thông minh
     */
    private QuestionType classifyQuestion(String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();
        
        // Kiểm tra câu hỏi về tất cả danh mục
        if (lowerMessage.contains("tất cả danh mục") || 
            lowerMessage.contains("danh sách danh mục") ||
            lowerMessage.contains("các danh mục") ||
            lowerMessage.contains("danh mục nào") ||
            lowerMessage.contains("loại sản phẩm nào") ||
            lowerMessage.contains("all categories") ||
            lowerMessage.contains("list categories") ||
            lowerMessage.contains("what categories")) {
            return QuestionType.ALL_CATEGORIES;
        }
        
        // Kiểm tra câu hỏi về tất cả sản phẩm
        if (lowerMessage.contains("tất cả sản phẩm") || 
            lowerMessage.contains("danh sách sản phẩm") ||
            lowerMessage.contains("show me all products") ||
            lowerMessage.contains("list all products")) {
            return QuestionType.ALL_PRODUCTS;
        }
        
        // Kiểm tra câu hỏi về sản phẩm mới
        if (lowerMessage.contains("sản phẩm mới") || 
            lowerMessage.contains("mới nhất") ||
            lowerMessage.contains("new products") ||
            lowerMessage.contains("latest products")) {
            return QuestionType.NEW_PRODUCTS;
        }
        
        // Kiểm tra câu hỏi về sản phẩm khuyến mại
        if (lowerMessage.contains("khuyến mại") || 
            lowerMessage.contains("giảm giá") ||
            lowerMessage.contains("sale") ||
            lowerMessage.contains("discount") ||
            lowerMessage.contains("off")) {
            return QuestionType.DISCOUNTED_PRODUCTS;
        }
        
        // Kiểm tra câu hỏi về tổng quan cửa hàng
        if (lowerMessage.contains("cửa hàng") || 
            lowerMessage.contains("shop") ||
            lowerMessage.contains("thông tin") ||
            lowerMessage.contains("overview") ||
            lowerMessage.contains("giới thiệu")) {
            return QuestionType.STORE_OVERVIEW;
        }
        
        // Kiểm tra câu hỏi về sản phẩm trong danh mục cụ thể
        if ((lowerMessage.contains("tất cả sản phẩm") && lowerMessage.contains("danh mục")) ||
            (lowerMessage.contains("sản phẩm") && lowerMessage.contains("danh mục")) ||
            (lowerMessage.contains("all products") && lowerMessage.contains("category")) ||
            (lowerMessage.contains("products") && lowerMessage.contains("category"))) {
            return QuestionType.CATEGORY_SPECIFIC;
        }
        
        // Kiểm tra câu hỏi về danh mục (chung chung)
        if (lowerMessage.contains("danh mục") || 
            lowerMessage.contains("category") ||
            lowerMessage.contains("loại sản phẩm")) {
            return QuestionType.CATEGORY_SPECIFIC;
        }
        
        // Kiểm tra câu hỏi về giá
        if (isPriceQuery(lowerMessage)) {
            return QuestionType.PRICE_QUERY;
        }
        
        // Kiểm tra câu hỏi về đơn hàng
        if (lowerMessage.contains("đơn hàng") || 
            lowerMessage.contains("order") ||
            lowerMessage.contains("hóa đơn") ||
            lowerMessage.contains("invoice")) {
            return QuestionType.ORDER_QUERY;
        }
        
        // Kiểm tra câu hỏi về trạng thái đơn hàng
        if (lowerMessage.contains("trạng thái đơn hàng") || 
            lowerMessage.contains("order status") ||
            lowerMessage.contains("đơn hàng của tôi") ||
            lowerMessage.contains("my order") ||
            lowerMessage.contains("đơn hàng đang xử lý") ||
            lowerMessage.contains("đơn hàng đã giao")) {
            return QuestionType.ORDER_STATUS;
        }
        
        // Kiểm tra câu hỏi về lịch sử đơn hàng
        if (lowerMessage.contains("lịch sử đơn hàng") || 
            lowerMessage.contains("order history") ||
            lowerMessage.contains("đơn hàng cũ") ||
            lowerMessage.contains("previous orders")) {
            return QuestionType.ORDER_HISTORY;
        }
        
        // Kiểm tra câu hỏi về hủy đơn hàng
        if (lowerMessage.contains("hủy đơn hàng") || 
            lowerMessage.contains("cancel order") ||
            lowerMessage.contains("hủy đơn") ||
            lowerMessage.contains("cancel")) {
            return QuestionType.ORDER_CANCEL;
        }
        
        // Kiểm tra câu hỏi về theo dõi đơn hàng
        if (lowerMessage.contains("theo dõi đơn hàng") || 
            lowerMessage.contains("track order") ||
            lowerMessage.contains("vị trí đơn hàng") ||
            lowerMessage.contains("đơn hàng ở đâu")) {
            return QuestionType.ORDER_TRACKING;
        }
        
        // Kiểm tra câu hỏi về giỏ hàng
        if (lowerMessage.contains("giỏ hàng") || 
            lowerMessage.contains("cart") ||
            lowerMessage.contains("giỏ") ||
            lowerMessage.contains("shopping cart")) {
            return QuestionType.CART_QUERY;
        }
        
        // Kiểm tra câu hỏi về thanh toán
        if (lowerMessage.contains("thanh toán") || 
            lowerMessage.contains("payment") ||
            lowerMessage.contains("tiền") ||
            lowerMessage.contains("money") ||
            lowerMessage.contains("sepay") ||
            lowerMessage.contains("chuyển khoản")) {
            return QuestionType.PAYMENT_QUERY;
        }
        
        // Kiểm tra câu hỏi về giao hàng
        if (lowerMessage.contains("giao hàng") || 
            lowerMessage.contains("shipping") ||
            lowerMessage.contains("vận chuyển") ||
            lowerMessage.contains("delivery") ||
            lowerMessage.contains("ship")) {
            return QuestionType.SHIPPING_QUERY;
        }
        
        // Kiểm tra câu hỏi về sản phẩm cụ thể
        if (lowerMessage.contains("sản phẩm") || 
            lowerMessage.contains("product") ||
            lowerMessage.contains("chi tiết") ||
            lowerMessage.contains("details") ||
            lowerMessage.contains("tìm") ||
            lowerMessage.contains("search")) {
            return QuestionType.PRODUCT_SEARCH;
        }
        
        // Mặc định là câu hỏi chung
        return QuestionType.GENERAL_QUESTION;
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
    
    // ==================== CÁC PHƯƠNG THỨC XỬ LÝ ORDER ====================
    
    /**
     * Lấy thông tin tổng quan về đơn hàng
     */
    private String getOrderInfo(User user) {
        try {
            List<HoaDon> orders = hoaDonService.getUserOrders(user);
            if (orders.isEmpty()) {
                return "📦 Bạn chưa có đơn hàng nào.\n\n" +
                       "💡 Để tạo đơn hàng:\n" +
                       "• Thêm sản phẩm vào giỏ hàng\n" +
                       "• Đi đến trang thanh toán\n" +
                       "• Hoàn tất thông tin giao hàng\n" +
                       "• Chọn phương thức thanh toán";
            }
            
            StringBuilder info = new StringBuilder();
            info.append("📦 THÔNG TIN ĐƠN HÀNG CỦA BẠN:\n\n");
            info.append("📊 Tổng số đơn hàng: ").append(orders.size()).append("\n\n");
            
            // Hiển thị 5 đơn hàng gần nhất
            int count = 0;
            for (HoaDon order : orders) {
                if (count >= 5) break;
                
                info.append("🛍️ Đơn hàng #").append(order.getId()).append("\n");
                info.append("────────────────────────────────────\n");
                info.append("📅 Ngày tạo: ").append(order.getNgayTao()).append("\n");
                info.append("💰 Tổng tiền: ").append(formatPrice(order.getTongTien())).append("\n");
                info.append("📋 Trạng thái: ").append(getOrderStatusText(order.getTrangThai())).append("\n");
                info.append("💳 Thanh toán: ").append(getPaymentMethodText(order.getLoaiThanhToan())).append("\n");
                info.append("🏠 Địa chỉ: ").append(order.getDiaChiGiaoHang()).append("\n");
                info.append("👤 Người nhận: ").append(order.getTenNguoiNhan()).append("\n");
                info.append("📞 SĐT: ").append(order.getSoDienThoaiGiaoHang()).append("\n");
                info.append("🔗 Xem chi tiết: /orders/").append(order.getId()).append("\n\n");
                count++;
            }
            
            if (orders.size() > 5) {
                info.append("... và ").append(orders.size() - 5).append(" đơn hàng khác\n\n");
            }
            
            info.append("💡 Bạn có thể hỏi:\n");
            info.append("• 'Trạng thái đơn hàng #123'\n");
            info.append("• 'Hủy đơn hàng #123'\n");
            info.append("• 'Theo dõi đơn hàng #123'\n");
            info.append("• 'Lịch sử đơn hàng'\n");
            
            return info.toString();
        } catch (Exception e) {
            return "❌ Không thể lấy thông tin đơn hàng. Vui lòng thử lại sau hoặc liên hệ hotline: 0982172169";
        }
    }
    
    /**
     * Lấy thông tin trạng thái đơn hàng
     */
    private String getOrderStatusInfo(User user, String userMessage) {
        try {
            // Tìm ID đơn hàng trong câu hỏi
            Integer orderId = extractOrderId(userMessage);
            if (orderId == null) {
                return "❌ Vui lòng cung cấp ID đơn hàng. Ví dụ: 'Trạng thái đơn hàng #123'";
            }
            
            HoaDon order = hoaDonService.getUserOrderById(user, orderId);
            if (order == null) {
                return "❌ Không tìm thấy đơn hàng #" + orderId + " hoặc bạn không có quyền xem đơn hàng này.";
            }
            
            StringBuilder info = new StringBuilder();
            info.append("📦 TRẠNG THÁI ĐƠN HÀNG #").append(order.getId()).append("\n\n");
            info.append("📅 Ngày tạo: ").append(order.getNgayTao()).append("\n");
            info.append("📋 Trạng thái: ").append(getOrderStatusText(order.getTrangThai())).append("\n");
            info.append("💰 Tổng tiền: ").append(formatPrice(order.getTongTien())).append("\n");
            
            if (order.getTongTienSauGiamGia() != null) {
                info.append("💵 Sau giảm giá: ").append(formatPrice(order.getTongTienSauGiamGia())).append("\n");
            }
            
            info.append("💳 Phương thức: ").append(getPaymentMethodText(order.getLoaiThanhToan())).append("\n");
            info.append("🏠 Địa chỉ: ").append(order.getDiaChiGiaoHang()).append("\n");
            info.append("👤 Người nhận: ").append(order.getTenNguoiNhan()).append("\n");
            info.append("📞 SĐT: ").append(order.getSoDienThoaiGiaoHang()).append("\n");
            
            if (order.getGhiChu() != null && !order.getGhiChu().trim().isEmpty()) {
                info.append("📝 Ghi chú: ").append(order.getGhiChu()).append("\n");
            }
            
            info.append("\n💡 Các hành động có thể thực hiện:\n");
            if ("PENDING".equals(order.getTrangThai()) || "PROCESSING".equals(order.getTrangThai())) {
                info.append("• Hủy đơn hàng: 'Hủy đơn hàng #").append(order.getId()).append("'\n");
            }
            info.append("• Xem chi tiết: /orders/").append(order.getId()).append("\n");
            info.append("• Liên hệ hỗ trợ: 0982172169\n");
            
            return info.toString();
        } catch (Exception e) {
            return "❌ Không thể lấy thông tin trạng thái đơn hàng. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Lấy lịch sử đơn hàng
     */
    private String getOrderHistoryInfo(User user) {
        try {
            List<HoaDon> orders = hoaDonService.getUserOrders(user);
            if (orders.isEmpty()) {
                return "📦 Bạn chưa có đơn hàng nào trong lịch sử.";
            }
            
            StringBuilder info = new StringBuilder();
            info.append("📚 LỊCH SỬ ĐƠN HÀNG:\n\n");
            
            // Nhóm đơn hàng theo trạng thái
            long pendingCount = orders.stream().filter(o -> "PENDING".equals(o.getTrangThai())).count();
            long processingCount = orders.stream().filter(o -> "PROCESSING".equals(o.getTrangThai())).count();
            long shippedCount = orders.stream().filter(o -> "SHIPPED".equals(o.getTrangThai())).count();
            long deliveredCount = orders.stream().filter(o -> "DELIVERED".equals(o.getTrangThai())).count();
            long cancelledCount = orders.stream().filter(o -> "CANCELLED".equals(o.getTrangThai())).count();
            
            info.append("📊 THỐNG KÊ:\n");
            info.append("• Đang chờ xử lý: ").append(pendingCount).append(" đơn\n");
            info.append("• Đang xử lý: ").append(processingCount).append(" đơn\n");
            info.append("• Đang giao hàng: ").append(shippedCount).append(" đơn\n");
            info.append("• Đã giao hàng: ").append(deliveredCount).append(" đơn\n");
            info.append("• Đã hủy: ").append(cancelledCount).append(" đơn\n\n");
            
            info.append("🛍️ ĐƠN HÀNG GẦN NHẤT:\n");
            int count = 0;
            for (HoaDon order : orders) {
                if (count >= 10) break;
                
                info.append("• #").append(order.getId())
                    .append(" - ").append(getOrderStatusText(order.getTrangThai()))
                    .append(" - ").append(formatPrice(order.getTongTien()))
                    .append(" - ").append(order.getNgayTao()).append("\n");
                count++;
            }
            
            if (orders.size() > 10) {
                info.append("... và ").append(orders.size() - 10).append(" đơn hàng khác\n");
            }
            
            info.append("\n💡 Để xem chi tiết: 'Trạng thái đơn hàng #123'\n");
            
            return info.toString();
        } catch (Exception e) {
            return "❌ Không thể lấy lịch sử đơn hàng. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Lấy thông tin hủy đơn hàng
     */
    private String getOrderCancelInfo(User user, String userMessage) {
        try {
            Integer orderId = extractOrderId(userMessage);
            if (orderId == null) {
                return "❌ Vui lòng cung cấp ID đơn hàng. Ví dụ: 'Hủy đơn hàng #123'";
            }
            
            HoaDon order = hoaDonService.getUserOrderById(user, orderId);
            if (order == null) {
                return "❌ Không tìm thấy đơn hàng #" + orderId + " hoặc bạn không có quyền xem đơn hàng này.";
            }
            
            // Kiểm tra trạng thái đơn hàng
            if ("DELIVERED".equals(order.getTrangThai())) {
                return "❌ Không thể hủy đơn hàng đã giao thành công.";
            }
            
            if ("CANCELLED".equals(order.getTrangThai())) {
                return "ℹ️ Đơn hàng #" + orderId + " đã được hủy trước đó.";
            }
            
            if ("SHIPPED".equals(order.getTrangThai())) {
                return "⚠️ Đơn hàng #" + orderId + " đang được giao hàng. Vui lòng liên hệ hotline: 0982172169 để được hỗ trợ.";
            }
            
            StringBuilder info = new StringBuilder();
            info.append("🛑 HỦY ĐƠN HÀNG #").append(order.getId()).append("\n\n");
            info.append("📋 Thông tin đơn hàng:\n");
            info.append("• Ngày tạo: ").append(order.getNgayTao()).append("\n");
            info.append("• Tổng tiền: ").append(formatPrice(order.getTongTien())).append("\n");
            info.append("• Trạng thái: ").append(getOrderStatusText(order.getTrangThai())).append("\n\n");
            
            info.append("⚠️ LƯU Ý QUAN TRỌNG:\n");
            info.append("• Hủy đơn hàng không thể hoàn tác\n");
            info.append("• Tiền sẽ được hoàn lại trong 3-5 ngày làm việc\n");
            info.append("• Sản phẩm sẽ được trả lại kho\n\n");
            
            info.append("🔧 ĐỂ HỦY ĐƠN HÀNG:\n");
            info.append("1. Truy cập: /orders/").append(order.getId()).append("\n");
            info.append("2. Click nút 'Hủy đơn hàng'\n");
            info.append("3. Nhập lý do hủy\n");
            info.append("4. Xác nhận hủy\n\n");
            
            info.append("📞 Cần hỗ trợ? Gọi: 0982172169\n");
            
            return info.toString();
        } catch (Exception e) {
            return "❌ Không thể xử lý yêu cầu hủy đơn hàng. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Lấy thông tin theo dõi đơn hàng
     */
    private String getOrderTrackingInfo(User user, String userMessage) {
        try {
            Integer orderId = extractOrderId(userMessage);
            if (orderId == null) {
                return "❌ Vui lòng cung cấp ID đơn hàng. Ví dụ: 'Theo dõi đơn hàng #123'";
            }
            
            HoaDon order = hoaDonService.getUserOrderById(user, orderId);
            if (order == null) {
                return "❌ Không tìm thấy đơn hàng #" + orderId + " hoặc bạn không có quyền xem đơn hàng này.";
            }
            
            StringBuilder info = new StringBuilder();
            info.append("📍 THEO DÕI ĐƠN HÀNG #").append(order.getId()).append("\n\n");
            
            // Hiển thị trạng thái hiện tại
            String status = order.getTrangThai();
            info.append("📋 TRẠNG THÁI HIỆN TẠI: ").append(getOrderStatusText(status)).append("\n\n");
            
            // Hiển thị timeline theo trạng thái
            info.append("📅 TIMELINE ĐƠN HÀNG:\n");
            info.append("✅ ").append(order.getNgayTao()).append(" - Đơn hàng được tạo\n");
            
            if ("PROCESSING".equals(status) || "SHIPPED".equals(status) || "DELIVERED".equals(status)) {
                info.append("✅ Đang xử lý - Xác nhận thanh toán\n");
            }
            
            if ("SHIPPED".equals(status) || "DELIVERED".equals(status)) {
                info.append("✅ Đang giao hàng - Đơn hàng đã được gửi đi\n");
            }
            
            if ("DELIVERED".equals(status)) {
                info.append("✅ Đã giao hàng - Hoàn thành\n");
            }
            
            if ("CANCELLED".equals(status)) {
                info.append("❌ Đã hủy - Đơn hàng bị hủy\n");
            }
            
            info.append("\n📦 THÔNG TIN GIAO HÀNG:\n");
            info.append("• Người nhận: ").append(order.getTenNguoiNhan()).append("\n");
            info.append("• SĐT: ").append(order.getSoDienThoaiGiaoHang()).append("\n");
            info.append("• Địa chỉ: ").append(order.getDiaChiGiaoHang()).append("\n");
            
            info.append("\n💡 CÁC BƯỚC TIẾP THEO:\n");
            if ("PENDING".equals(status)) {
                info.append("• Chờ xác nhận thanh toán\n");
                info.append("• Kiểm tra email để xác nhận\n");
            } else if ("PROCESSING".equals(status)) {
                info.append("• Đơn hàng đang được chuẩn bị\n");
                info.append("• Sẽ được giao trong 1-2 ngày\n");
            } else if ("SHIPPED".equals(status)) {
                info.append("• Đơn hàng đang được giao\n");
                info.append("• Chuẩn bị nhận hàng\n");
            } else if ("DELIVERED".equals(status)) {
                info.append("• Đơn hàng đã giao thành công\n");
                info.append("• Cảm ơn bạn đã mua hàng!\n");
            }
            
            info.append("\n📞 Cần hỗ trợ? Gọi: 0982172169\n");
            
            return info.toString();
        } catch (Exception e) {
            return "❌ Không thể theo dõi đơn hàng. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Lấy thông tin giỏ hàng
     */
    private String getCartInfo(User user) {
        return "🛒 THÔNG TIN GIỎ HÀNG:\n\n" +
               "💡 Để xem giỏ hàng:\n" +
               "• Truy cập: /cart\n" +
               "• Xem sản phẩm đã thêm\n" +
               "• Cập nhật số lượng\n" +
               "• Áp dụng mã giảm giá\n" +
               "• Tiến hành thanh toán\n\n" +
               "🔧 Các thao tác:\n" +
               "• Thêm sản phẩm: Click 'Thêm vào giỏ'\n" +
               "• Xóa sản phẩm: Click 'Xóa'\n" +
               "• Cập nhật: Thay đổi số lượng\n" +
               "• Thanh toán: Click 'Thanh toán'\n\n" +
               "📞 Cần hỗ trợ? Gọi: 0982172169";
    }
    
    /**
     * Lấy thông tin thanh toán
     */
    private String getPaymentInfo(String userMessage) {
        return "💳 THÔNG TIN THANH TOÁN:\n\n" +
               "🏦 PHƯƠNG THỨC THANH TOÁN:\n" +
               "• Chuyển khoản ngân hàng (SePay)\n" +
               "• Thanh toán khi nhận hàng (COD)\n" +
               "• Ví điện tử\n\n" +
               "💰 CHÍNH SÁCH GIÁ:\n" +
               "• Miễn phí ship trên 100k\n" +
               "• Áp dụng mã giảm giá\n" +
               "• Giá đã bao gồm VAT\n\n" +
               "🔒 BẢO MẬT:\n" +
               "• Thông tin thanh toán được mã hóa\n" +
               "• Không lưu trữ thông tin thẻ\n" +
               "• Tuân thủ chuẩn PCI DSS\n\n" +
               "📞 Hỗ trợ thanh toán: 0982172169";
    }
    
    /**
     * Lấy thông tin giao hàng
     */
    private String getShippingInfo(String userMessage) {
        return "🚚 THÔNG TIN GIAO HÀNG:\n\n" +
               "📦 CHÍNH SÁCH GIAO HÀNG:\n" +
               "• Miễn phí ship trên 100k\n" +
               "• Phí ship 30k cho đơn dưới 100k\n" +
               "• Giao hàng toàn quốc\n" +
               "• Thời gian: 1-3 ngày làm việc\n\n" +
               "📍 KHU VỰC GIAO HÀNG:\n" +
               "• Hà Nội: 1-2 ngày\n" +
               "• TP.HCM: 1-2 ngày\n" +
               "• Các tỉnh khác: 2-3 ngày\n\n" +
               "📞 LIÊN HỆ GIAO HÀNG:\n" +
               "• Hotline: 0982172169\n" +
               "• Email: shipping@shopreid.com\n" +
               "• Thời gian: 8:00 - 18:00 (T2-T7)\n\n" +
               "⚠️ LƯU Ý:\n" +
               "• Kiểm tra hàng trước khi thanh toán\n" +
               "• Giữ hóa đơn để đổi trả\n" +
               "• Thời gian đổi trả: 7 ngày";
    }
    
    // ==================== CÁC PHƯƠNG THỨC HỖ TRỢ ====================
    
    private Integer extractOrderId(String userMessage) {
        // Tìm số ID trong câu hỏi
        String pattern = "#(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(userMessage);
        
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Tìm số đơn hàng không có dấu #
        pattern = "đơn hàng\\s+(\\d+)";
        p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        m = p.matcher(userMessage);
        
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    private String getOrderStatusText(String status) {
        if (status == null) return "Không xác định";
        
        switch (status.toUpperCase()) {
            case "PENDING": return "⏳ Đang chờ xử lý";
            case "PROCESSING": return "🔄 Đang xử lý";
            case "SHIPPED": return "🚚 Đang giao hàng";
            case "DELIVERED": return "✅ Đã giao hàng";
            case "CANCELLED": return "❌ Đã hủy";
            case "REFUNDED": return "💰 Đã hoàn tiền";
            default: return "❓ " + status;
        }
    }
    
    private String getPaymentMethodText(String method) {
        if (method == null) return "Chưa xác định";
        
        switch (method.toUpperCase()) {
            case "BANK_TRANSFER": return "🏦 Chuyển khoản ngân hàng";
            case "COD": return "💰 Thanh toán khi nhận hàng";
            case "SEPAY": return "💳 SePay";
            case "WALLET": return "📱 Ví điện tử";
            default: return "💳 " + method;
        }
    }
    
    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "0 VNĐ";
        return String.format("%,d VNĐ", price.intValue());
    }
}
