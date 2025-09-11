# Hệ thống Thông báo Real-time với WebSocket

## Tổng quan
Hệ thống thông báo real-time được xây dựng sử dụng Spring WebSocket và SockJS để gửi thông báo tức thời cho cả user và admin khi có sự kiện liên quan đến đơn hàng.

## Tính năng chính

### 1. Thông báo cho Admin
- **Khi có đơn hàng mới**: Admin sẽ nhận được thông báo ngay lập tức khi user đặt hàng
- **Thông báo hiển thị**: Tiêu đề, nội dung, thời gian tạo, ID đơn hàng

### 2. Thông báo cho User
- **Khi đặt hàng thành công**: User nhận thông báo xác nhận đơn hàng
- **Khi admin thay đổi trạng thái**: User được thông báo khi trạng thái đơn hàng thay đổi
- **Các trạng thái**: PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED

### 3. Giao diện thông báo
- **Toast notification**: Thông báo popup tự động ẩn sau 5 giây
- **Badge đếm**: Hiển thị số thông báo chưa đọc
- **Dropdown**: Xem danh sách thông báo gần đây
- **Trang quản lý**: Xem tất cả thông báo và đánh dấu đã đọc

## Cấu trúc hệ thống

### Backend Components

#### 1. Model
- `Notification.java`: Entity lưu trữ thông báo
- Các trường: id, title, message, type, isRead, createdAt, orderId, user, adminCreated

#### 2. Repository
- `NotificationRepository.java`: Interface JPA cho các thao tác database
- Các method: findByUser, findByUserAndIsReadFalse, countByUserAndIsReadFalse, findAdminNotifications

#### 3. Service
- `NotificationService.java`: Interface định nghĩa các chức năng
- `NotificationServiceImpl.java`: Implementation với WebSocket integration

#### 4. WebSocket Configuration
- `WebSocketConfig.java`: Cấu hình WebSocket endpoints và message broker
- Endpoint: `/ws` với SockJS fallback
- Topics: `/topic/admin/notifications`, `/queue/notifications`

#### 5. Controllers
- `NotificationController.java`: REST API cho thông báo
- `WebSocketController.java`: WebSocket message handling

### Frontend Components

#### 1. JavaScript
- `websocket.js`: WebSocket client với SockJS
- Tự động kết nối, reconnect, xử lý message
- Toast notification, dropdown management

#### 2. CSS
- `notifications.css`: Styling cho thông báo
- Toast, dropdown, badge, responsive design

#### 3. Templates
- `user/notifications.html`: Trang thông báo cho user
- `admin/notifications.html`: Trang thông báo cho admin

## Cách sử dụng

### 1. Khởi động ứng dụng
```bash
mvn spring-boot:run
```

### 2. Truy cập thông báo
- **User**: `http://localhost:8080/notifications`
- **Admin**: `http://localhost:8080/notifications/admin`

### 3. Test thông báo
1. Đăng nhập với tài khoản user
2. Đặt một đơn hàng
3. Admin sẽ nhận được thông báo real-time
4. Đăng nhập admin và thay đổi trạng thái đơn hàng
5. User sẽ nhận được thông báo cập nhật

## API Endpoints

### User Notifications
- `GET /notifications` - Trang thông báo user
- `POST /notifications/mark-read/{id}` - Đánh dấu đã đọc
- `POST /notifications/mark-all-read` - Đánh dấu tất cả đã đọc
- `GET /notifications/api/unread-count` - Lấy số thông báo chưa đọc

### Admin Notifications
- `GET /notifications/admin` - Trang thông báo admin

## WebSocket Events

### User Events
- **Subscribe**: `/user/queue/notifications`
- **Message**: Notification object với type, title, message, orderId

### Admin Events
- **Subscribe**: `/topic/admin/notifications`
- **Message**: Notification object cho admin

## Cấu hình

### 1. Database
Hệ thống tự động tạo bảng `notifications` khi khởi động.

### 2. WebSocket
- Endpoint: `/ws`
- CORS: Cho phép tất cả origins
- Fallback: SockJS

### 3. Security
- User notifications yêu cầu authentication
- Admin notifications yêu cầu role ADMIN

## Troubleshooting

### 1. WebSocket không kết nối
- Kiểm tra console browser để xem lỗi
- Đảm bảo SockJS library được load
- Kiểm tra firewall/proxy settings

### 2. Thông báo không hiển thị
- Kiểm tra WebSocket connection
- Kiểm tra user authentication
- Kiểm tra database connection

### 3. Badge không cập nhật
- Kiểm tra API `/notifications/api/unread-count`
- Kiểm tra JavaScript console
- Refresh trang để load lại

## Mở rộng

### 1. Thêm loại thông báo mới
1. Cập nhật `Notification` model
2. Thêm logic trong service
3. Cập nhật frontend để hiển thị

### 2. Thêm thông báo push
1. Tích hợp Firebase Cloud Messaging
2. Cập nhật service để gửi push notification
3. Thêm permission request

### 3. Thêm email notification
1. Tích hợp Spring Mail
2. Tạo email templates
3. Cập nhật service để gửi email

## Lưu ý quan trọng

1. **Performance**: Hệ thống sử dụng in-memory message broker, phù hợp cho ứng dụng nhỏ
2. **Scalability**: Để scale, cần sử dụng Redis hoặc RabbitMQ
3. **Security**: Cần thêm authentication cho WebSocket connections
4. **Monitoring**: Nên thêm logging và monitoring cho WebSocket connections
