# Hướng dẫn cài đặt OAuth2 Google Login

## Tổng quan
Dự án đã được tích hợp chức năng đăng nhập bằng Google OAuth2. Người dùng có thể đăng nhập bằng tài khoản Google của họ thay vì tạo tài khoản mới.

## Cấu hình Google OAuth2

### 1. Tạo Google OAuth2 Credentials

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Vào "APIs & Services" > "Credentials"
4. Click "Create Credentials" > "OAuth 2.0 Client IDs"
5. Chọn "Web application"
6. Điền thông tin:
   - **Name**: Tên ứng dụng của bạn
   - **Authorized JavaScript origins**: `http://localhost:8080`
   - **Authorized redirect URIs**: `http://localhost:8080/login/oauth2/code/google`
7. Lưu lại **Client ID** và **Client Secret**

### 2. Cập nhật application.properties

Cập nhật thông tin trong file `src/main/resources/application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

## Cách sử dụng

### 1. Đăng nhập bằng Google
- Truy cập trang login: `http://localhost:8080/login`
- Click nút "Đăng nhập bằng Google"
- Chọn tài khoản Google và cấp quyền
- Hệ thống sẽ tự động tạo tài khoản mới hoặc đăng nhập vào tài khoản có sẵn

### 2. Tài khoản mặc định
- **Admin**: `admin/admin123`
- **User**: Được tạo tự động khi đăng nhập bằng Google

## Cấu trúc code

### Các file chính:

1. **CustomOAuth2UserService.java**: Xử lý thông tin user từ Google
2. **SecurityConfig.java**: Cấu hình OAuth2 và security
3. **DataInitializationService.java**: Khởi tạo roles và admin user
4. **login.html**: Template với nút Google login
5. **oauth2-login.css**: Style cho nút Google login

### Luồng hoạt động:

1. User click nút "Đăng nhập bằng Google"
2. Redirect đến Google OAuth2
3. User xác thực và cấp quyền
4. Google callback về `/login/oauth2/code/google`
5. `CustomOAuth2UserService` xử lý thông tin user
6. Tạo user mới hoặc đăng nhập user có sẵn
7. Redirect theo role (USER → `/home`, ADMIN → `/admin/product`)

## Lưu ý bảo mật

1. **Client Secret**: Không commit client secret vào git
2. **HTTPS**: Sử dụng HTTPS trong production
3. **Domain**: Cập nhật authorized domains khi deploy
4. **Session**: Session được invalidate khi logout

## Troubleshooting

### Lỗi thường gặp:

1. **"Invalid redirect URI"**: Kiểm tra redirect URI trong Google Console
2. **"Client ID not found"**: Kiểm tra client ID trong application.properties
3. **"Role not found"**: Chạy lại ứng dụng để khởi tạo roles

### Debug:

Thêm log trong `CustomOAuth2UserService` để debug:
```java
System.out.println("Email: " + email);
System.out.println("Provider: " + provider);
```

## Production Deployment

1. Cập nhật redirect URIs trong Google Console
2. Sử dụng HTTPS
3. Cấu hình session management
4. Backup database
5. Monitor logs 