class AdminNotificationWebSocket {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        this.isAdmin = true; // Admin flag
    }

    connect() {
        if (this.isConnected) return;

        try {
            // Sử dụng SockJS để kết nối WebSocket
            this.socket = new SockJS('/ws');
            
            this.socket.onopen = () => {
                console.log('Admin WebSocket connected');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this.subscribeToNotifications();
            };

            this.socket.onclose = () => {
                console.log('Admin WebSocket disconnected');
                this.isConnected = false;
                this.attemptReconnect();
            };

            this.socket.onerror = (error) => {
                console.error('Admin WebSocket error:', error);
            };

            this.socket.onmessage = (event) => {
                this.handleMessage(event);
            };

        } catch (error) {
            console.error('Failed to connect to Admin WebSocket:', error);
            this.attemptReconnect();
        }
    }

    subscribeToNotifications() {
        if (this.socket && this.isConnected) {
            // Subscribe to admin notifications
            this.socket.send(JSON.stringify({
                command: 'subscribe',
                destination: '/topic/admin/notifications'
            }));
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
            console.error('Error parsing Admin WebSocket message:', error);
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

        // Cập nhật danh sách thông báo nếu đang ở trang admin notifications
        if (window.location.pathname.includes('/notifications/admin')) {
            this.addNotificationToList(notification);
        }
    }

    createToast(notification) {
        const toast = document.createElement('div');
        toast.className = 'admin-notification-toast';
        toast.innerHTML = `
            <div class="admin-toast-header">
                <strong>${notification.title}</strong>
                <button type="button" class="admin-btn-close" onclick="this.parentElement.parentElement.remove()">&times;</button>
            </div>
            <div class="admin-toast-body">
                ${notification.message}
            </div>
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
        const notificationList = document.getElementById('admin-notification-list');
        if (notificationList) {
            const notificationItem = document.createElement('div');
            notificationItem.className = 'admin-notification-mini-item unread';
            notificationItem.innerHTML = `
                <div class="admin-notification-mini-content">
                    <h6>${notification.title}</h6>
                    <p>${notification.message}</p>
                    <small>${new Date(notification.createdAt).toLocaleString()}</small>
                </div>
            `;
            notificationList.insertBefore(notificationItem, notificationList.firstChild);
        }
    }

    updateNotificationCount() {
        // Cập nhật badge số thông báo chưa đọc cho admin
        fetch('/notifications/api/admin/unread-count')
            .then(response => response.json())
            .then(count => {
                const badge = document.getElementById('admin-notification-badge');
                if (badge) {
                    badge.textContent = count;
                    badge.style.display = count > 0 ? 'inline' : 'none';
                }
            })
            .catch(error => console.error('Error updating admin notification count:', error));
    }

    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Admin attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max admin reconnection attempts reached');
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
        // Load thông báo cho admin dropdown
        fetch('/notifications/api/admin/recent')
            .then(response => response.json())
            .then(notifications => {
                this.renderDropdownNotifications(notifications);
            })
            .catch(error => {
                console.error('Error loading admin dropdown notifications:', error);
                this.renderDropdownError();
            });
    }

    renderDropdownNotifications(notifications) {
        const container = document.getElementById('admin-notification-list-dropdown');
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
            <div class="admin-notification-mini-item ${notification.isRead ? '' : 'unread'}" 
                 onclick="markAdminNotificationAsRead(${notification.id})">
                <div class="admin-notification-mini-content">
                    <h6>${this.escapeHtml(notification.title)}</h6>
                    <p>${this.escapeHtml(notification.message)}</p>
                    <small>${this.formatTime(notification.createdAt)}</small>
                </div>
            </div>
        `).join('');

        container.innerHTML = html;
    }

    renderDropdownError() {
        const container = document.getElementById('admin-notification-list-dropdown');
        if (!container) return;

        container.innerHTML = `
            <div class="text-center p-4">
                <i class="fa fa-exclamation-triangle text-warning" style="font-size: 2rem;"></i>
                <p class="mt-2 mb-0 text-muted">Không thể tải thông báo</p>
                <button class="btn btn-sm btn-outline-primary mt-2" onclick="window.adminNotificationWebSocket.loadDropdownNotifications()">
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

// Khởi tạo Admin WebSocket khi trang load
document.addEventListener('DOMContentLoaded', function() {
    window.adminNotificationWebSocket = new AdminNotificationWebSocket();
    window.adminNotificationWebSocket.connect();
});

// Function để toggle admin dropdown
function toggleAdminNotificationDropdown(event) {
    event.preventDefault();
    const dropdown = document.getElementById('admin-notification-dropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
        
        // Load thông báo khi mở dropdown
        if (dropdown.classList.contains('show')) {
            window.adminNotificationWebSocket.loadDropdownNotifications();
        }
    }
}

// Function để load admin dropdown notifications khi hover
function loadAdminDropdownNotifications() {
    if (window.adminNotificationWebSocket) {
        window.adminNotificationWebSocket.loadDropdownNotifications();
    }
}

// Function để mark admin notification as read
function markAdminNotificationAsRead(notificationId) {
    fetch(`/notifications/mark-read/${notificationId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.text())
    .then(data => {
        // Cập nhật UI
        const notificationItem = event.target.closest('.admin-notification-mini-item');
        if (notificationItem) {
            notificationItem.classList.remove('unread');
        }
        // Cập nhật số thông báo chưa đọc
        if (window.adminNotificationWebSocket) {
            window.adminNotificationWebSocket.updateNotificationCount();
        }
    })
    .catch(error => console.error('Error:', error));
}

// Đóng admin dropdown khi click bên ngoài
document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('admin-notification-dropdown');
    const bell = document.querySelector('.admin-notification-dropdown > a');
    
    if (dropdown && bell && !dropdown.contains(event.target) && !bell.contains(event.target)) {
        dropdown.classList.remove('show');
    }
});

// Kết nối lại khi user focus vào tab
window.addEventListener('focus', function() {
    if (window.adminNotificationWebSocket && !window.adminNotificationWebSocket.isConnected) {
        window.adminNotificationWebSocket.connect();
    }
});
