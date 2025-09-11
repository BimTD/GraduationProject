class NotificationWebSocket {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        this.isAdmin = false; // Add admin flag
    }

    connect() {
        if (this.isConnected) return;

        try {
            // Sử dụng SockJS để kết nối WebSocket
            this.socket = new SockJS('/ws');
            
            this.socket.onopen = () => {
                console.log('WebSocket connected');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this.subscribeToNotifications();
            };

            this.socket.onclose = () => {
                console.log('WebSocket disconnected');
                this.isConnected = false;
                this.attemptReconnect();
            };

            this.socket.onerror = (error) => {
                console.error('WebSocket error:', error);
            };

            this.socket.onmessage = (event) => {
                this.handleMessage(event);
            };

        } catch (error) {
            console.error('Failed to connect to WebSocket:', error);
            this.attemptReconnect();
        }
    }

    subscribeToNotifications() {
        if (this.socket && this.isConnected) {
            // Subscribe to user notifications
            this.socket.send(JSON.stringify({
                command: 'subscribe',
                destination: '/user/queue/notifications'
            }));

            // Subscribe to admin notifications (if user is admin)
            if (this.isAdmin()) {
                this.socket.send(JSON.stringify({
                    command: 'subscribe',
                    destination: '/topic/admin/notifications'
                }));
            }
        }
    }

    handleMessage(event) {
        try {
            const data = JSON.parse(event.data);
            if (data.type === 'notification') {
                this.showNotification(data);
                this.updateNotificationCount();
            }
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    }

    showNotification(notification) {
        // Tạo thông báo toast
        const toast = this.createToast(notification);
        document.body.appendChild(toast);

        // Hiển thị thông báo
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            this.hideToast(toast);
        }, 5000);

        // Cập nhật danh sách thông báo nếu đang ở trang notifications
        if (window.location.pathname.includes('/notifications')) {
            this.addNotificationToList(notification);
        }
    }

    createToast(notification) {
        const toast = document.createElement('div');
        toast.className = 'notification-toast';
        toast.innerHTML = `
            <div class="toast-header">
                <strong>${notification.title}</strong>
                <button type="button" class="btn-close" onclick="this.parentElement.parentElement.remove()">&times;</button>
            </div>
            <div class="toast-body">
                ${notification.message}
            </div>
        `;

        // Thêm CSS cho toast
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            border: 1px solid #ddd;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 9999;
            max-width: 350px;
            opacity: 0;
            transform: translateX(100%);
            transition: all 0.3s ease;
        `;

        return toast;
    }

    hideToast(toast) {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentElement) {
                toast.parentElement.removeChild(toast);
            }
        }, 300);
    }

    addNotificationToList(notification) {
        const notificationList = document.getElementById('notification-list') || 
                                document.getElementById('admin-notification-list');
        if (notificationList) {
            const notificationItem = document.createElement('div');
            notificationItem.className = 'notification-item unread';
            notificationItem.innerHTML = `
                <div class="notification-content">
                    <h6>${notification.title}</h6>
                    <p>${notification.message}</p>
                    <small class="text-muted">${new Date(notification.createdAt).toLocaleString()}</small>
                </div>
            `;
            notificationList.insertBefore(notificationItem, notificationList.firstChild);
        }
    }

    updateNotificationCount() {
        // Cập nhật badge số thông báo chưa đọc cho user
        if (!this.isAdmin()) {
            fetch('/notifications/api/unread-count')
                .then(response => response.json())
                .then(count => {
                    const badge = document.getElementById('notification-badge');
                    if (badge) {
                        badge.textContent = count;
                        badge.style.display = count > 0 ? 'inline' : 'none';
                    }
                })
                .catch(error => console.error('Error updating notification count:', error));
        } else {
            // Cập nhật badge cho admin
            const badge = document.getElementById('admin-notification-badge');
            if (badge) {
                // Có thể implement logic đếm thông báo admin ở đây
                // Hiện tại chỉ hiển thị badge khi có thông báo mới
            }
        }
    }

    isAdmin() {
        // Kiểm tra xem user có phải admin không (có thể dựa vào URL hoặc data attribute)
        return window.location.pathname.includes('/admin') || 
               document.body.getAttribute('data-user-role') === 'ADMIN' ||
               this.isAdmin;
    }

    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        this.isConnected = false;
    }

    loadDropdownNotifications() {
        // Load thông báo cho dropdown
        fetch('/notifications/api/recent')
            .then(response => response.json())
            .then(notifications => {
                this.renderDropdownNotifications(notifications);
            })
            .catch(error => {
                console.error('Error loading dropdown notifications:', error);
                this.renderDropdownError();
            });
    }

    renderDropdownNotifications(notifications) {
        const container = document.getElementById('notification-list-dropdown');
        if (!container) return;

        if (notifications.length === 0) {
            container.innerHTML = `
                <div class="text-center p-4">
                    <i class="fa fa-bell-slash text-muted" style="font-size: 2rem;"></i>
                    <p class="mt-2 mb-0 text-muted">Chưa có thông báo nào</p>
                </div>
            `;
            return;
        }

        const html = notifications.map(notification => `
            <div class="notification-mini-item ${notification.isRead ? '' : 'unread'}" 
                 onclick="markAsRead(${notification.id})">
                <div class="notification-mini-content">
                    <h6>${this.escapeHtml(notification.title)}</h6>
                    <p>${this.escapeHtml(notification.message)}</p>
                    <small>${this.formatTime(notification.createdAt)}</small>
                </div>
            </div>
        `).join('');

        container.innerHTML = html;
    }

    renderDropdownError() {
        const container = document.getElementById('notification-list-dropdown');
        if (!container) return;

        container.innerHTML = `
            <div class="text-center p-4">
                <i class="fa fa-exclamation-triangle text-warning" style="font-size: 2rem;"></i>
                <p class="mt-2 mb-0 text-muted">Không thể tải thông báo</p>
                <button class="btn btn-sm btn-outline-primary mt-2" onclick="window.notificationWebSocket.loadDropdownNotifications()">
                    Thử lại
                </button>
            </div>
        `;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // 1 minute
            return 'Vừa xong';
        } else if (diff < 3600000) { // 1 hour
            return Math.floor(diff / 60000) + ' phút trước';
        } else if (diff < 86400000) { // 1 day
            return Math.floor(diff / 3600000) + ' giờ trước';
        } else {
            return date.toLocaleDateString('vi-VN', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }
    }
}

// Khởi tạo WebSocket khi trang load
document.addEventListener('DOMContentLoaded', function() {
    window.notificationWebSocket = new NotificationWebSocket();
    window.notificationWebSocket.connect();
});

// Function để toggle dropdown
function toggleNotificationDropdown(event) {
    event.preventDefault();
    const dropdown = document.getElementById('notification-dropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
        
        // Load thông báo khi mở dropdown
        if (dropdown.classList.contains('show')) {
            window.notificationWebSocket.loadDropdownNotifications();
        }
    }
}

// Function để load dropdown notifications khi hover
function loadDropdownNotifications() {
    if (window.notificationWebSocket) {
        window.notificationWebSocket.loadDropdownNotifications();
    }
}

// Function để mark notification as read
function markAsRead(notificationId) {
    fetch(`/notifications/mark-read/${notificationId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.text())
    .then(data => {
        // Cập nhật UI
        const notificationItem = event.target.closest('.notification-mini-item');
        if (notificationItem) {
            notificationItem.classList.remove('unread');
        }
        // Cập nhật số thông báo chưa đọc
        if (window.notificationWebSocket) {
            window.notificationWebSocket.updateNotificationCount();
        }
    })
    .catch(error => console.error('Error:', error));
}

// Đóng dropdown khi click bên ngoài
document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('notification-dropdown');
    const bell = document.querySelector('.notification-dropdown > a');
    
    if (dropdown && bell && !dropdown.contains(event.target) && !bell.contains(event.target)) {
        dropdown.classList.remove('show');
    }
});

// Kết nối lại khi user focus vào tab
window.addEventListener('focus', function() {
    if (window.notificationWebSocket && !window.notificationWebSocket.isConnected) {
        window.notificationWebSocket.connect();
    }
});
