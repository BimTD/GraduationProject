class Chatbot {
    constructor() {
        this.isOpen = false;
        this.socket = null;
        this.stompClient = null;
        this.init();
    }
    
    init() {
        this.createWidget();
        this.connectWebSocket();
        this.loadChatHistory();
    }
    
    createWidget() {
        // Tạo HTML cho chat widget
        const chatHTML = `
            <div id="chatbot-widget" class="chatbot-widget">
                <div id="chatbot-toggle" class="chatbot-toggle">
                    <i class="fas fa-comments"></i>
                    <span class="chatbot-badge" id="chatbot-badge" style="display: none;">1</span>
                </div>
                <div id="chatbot-container" class="chatbot-container">
                    <div class="chatbot-header">
                        <div>
                            <h4>🤖 Trợ lý AI</h4>
                            <div class="status">Đang hoạt động</div>
                        </div>
                        <button id="chatbot-close" class="chatbot-close">&times;</button>
                    </div>
                    <div id="chatbot-messages" class="chatbot-messages">
                        <div class="welcome-message">
                            👋 Xin chào! Tôi là trợ lý AI của cửa hàng. Tôi có thể giúp bạn tìm hiểu về sản phẩm, đơn hàng và các câu hỏi khác. Hãy hỏi tôi bất cứ điều gì!
                        </div>
                    </div>
                    <div class="chatbot-input">
                        <input type="text" id="chatbot-input-field" placeholder="Nhập tin nhắn của bạn...">
                        <button id="chatbot-send">📤</button>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', chatHTML);
        
        // Thêm event listeners
        document.getElementById('chatbot-toggle').addEventListener('click', () => this.toggleChat());
        document.getElementById('chatbot-close').addEventListener('click', () => this.closeChat());
        document.getElementById('chatbot-send').addEventListener('click', () => this.sendMessage());
        document.getElementById('chatbot-input-field').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
        
        // Auto-focus input khi mở chat
        document.getElementById('chatbot-input-field').addEventListener('focus', () => {
            this.scrollToBottom();
        });
        
        // Click outside để đóng chat
        document.addEventListener('click', (e) => {
            const widget = document.getElementById('chatbot-widget');
            if (this.isOpen && !widget.contains(e.target)) {
                this.closeChat();
            }
        });
        
        // Prevent chat container click from closing
        document.getElementById('chatbot-container').addEventListener('click', (e) => {
            e.stopPropagation();
        });
    }
    
    connectWebSocket() {
        this.socket = new SockJS('/chat');
        this.stompClient = Stomp.over(this.socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Chatbot WebSocket Connected: ' + frame);
            
            this.stompClient.subscribe('/topic/chat', (message) => {
                const response = JSON.parse(message.body);
                this.displayMessage(response.response, 'bot');
            });
        }, (error) => {
            console.error('WebSocket connection error:', error);
            // Retry connection after 3 seconds
            setTimeout(() => {
                this.connectWebSocket();
            }, 3000);
        });
    }
    
    toggleChat() {
        this.isOpen = !this.isOpen;
        const container = document.getElementById('chatbot-container');
        const toggle = document.getElementById('chatbot-toggle');
        
        if (this.isOpen) {
            container.classList.add('show');
            toggle.classList.add('active');
            // Focus vào input khi mở chat
            setTimeout(() => {
                document.getElementById('chatbot-input-field').focus();
            }, 400);
        } else {
            container.classList.remove('show');
            toggle.classList.remove('active');
        }
    }
    
    closeChat() {
        this.isOpen = false;
        const container = document.getElementById('chatbot-container');
        const toggle = document.getElementById('chatbot-toggle');
        container.classList.remove('show');
        toggle.classList.remove('active');
    }
    
    sendMessage() {
        const input = document.getElementById('chatbot-input-field');
        const message = input.value.trim();
        
        if (message) {
            this.displayMessage(message, 'user');
            
            // Hiển thị typing indicator
            this.showTypingIndicator();
            
            // Gửi qua WebSocket
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
                message: message,
                userId: this.getCurrentUserId()
            }));
            
            input.value = '';
        }
    }
    
    showTypingIndicator() {
        const messagesContainer = document.getElementById('chatbot-messages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chatbot-message bot typing-indicator-message';
        typingDiv.id = 'typing-indicator';
        
        typingDiv.innerHTML = `
            <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
            </div>
        `;
        
        messagesContainer.appendChild(typingDiv);
        this.scrollToBottom();
    }
    
    hideTypingIndicator() {
        const typingIndicator = document.getElementById('typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }
    
    displayMessage(message, type) {
        // Ẩn typing indicator nếu có
        this.hideTypingIndicator();
        
        const messagesContainer = document.getElementById('chatbot-messages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `chatbot-message ${type}`;
        
        // Format message với emoji và link
        const formattedMessage = this.formatMessage(message);
        
        messageDiv.innerHTML = `
            <div class="message-content">
                ${formattedMessage}
            </div>
            <div class="message-time">${new Date().toLocaleTimeString()}</div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }
    
    formatMessage(message) {
        // Thay thế URLs thành links
        const urlRegex = /(https?:\/\/[^\s]+)/g;
        let formatted = message.replace(urlRegex, '<a href="$1" target="_blank" style="color: #667eea; text-decoration: none;">$1</a>');
        
        // Thay thế product links thành clickable links
        const productLinkRegex = /\/product-details\/(\d+)/g;
        formatted = formatted.replace(productLinkRegex, '<a href="/product-details/$1" style="color: #667eea; text-decoration: none; font-weight: bold;">Xem sản phẩm #$1</a>');
        
        // Thay thế line breaks
        formatted = formatted.replace(/\n/g, '<br>');
        
        return formatted;
    }
    
    scrollToBottom() {
        const messagesContainer = document.getElementById('chatbot-messages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
    
    loadChatHistory() {
        fetch('/api/chat/history')
            .then(response => response.json())
            .then(messages => {
                if (messages && messages.length > 0) {
                    // Ẩn welcome message nếu có lịch sử chat
                    const welcomeMessage = document.querySelector('.welcome-message');
                    if (welcomeMessage) {
                        welcomeMessage.style.display = 'none';
                    }
                    
                    messages.forEach(msg => {
                        if (msg.messageType === 'USER') {
                            this.displayMessage(msg.message, 'user');
                        } else {
                            this.displayMessage(msg.response, 'bot');
                        }
                    });
                }
            })
            .catch(error => console.error('Error loading chat history:', error));
    }
    
    getCurrentUserId() {
        // Lấy user ID từ session hoặc token
        // Cần implement logic này dựa trên authentication system
        return 1; // Placeholder
    }
}

// Khởi tạo chatbot khi trang load
document.addEventListener('DOMContentLoaded', () => {
    new Chatbot();
});
