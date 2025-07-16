# Hướng dẫn sử dụng chức năng đăng ký tài khoản

## Tổng quan
Hệ thống đã được tích hợp chức năng đăng ký tài khoản với username và password. Người dùng có thể tạo tài khoản mới để sử dụng đầy đủ tính năng của hệ thống.

## Các tính năng

### 1. Đăng ký tài khoản mới
- **URL**: `http://localhost:8080/register`
- **Phương thức**: POST
- **Tham số**:
  - `username`: Tên đăng nhập (tối thiểu 3 ký tự)
  - `email`: Email hợp lệ
  - `password`: Mật khẩu (tối thiểu 6 ký tự)
  - `confirmPassword`: Xác nhận mật khẩu

### 2. Validation
- **Username**: Phải có ít nhất 3 ký tự, không được trùng
- **Email**: Phải đúng định dạng email, không được trùng
- **Password**: Phải có ít nhất 6 ký tự
- **Confirm Password**: Phải khớp với password

### 3. Bảo mật
- Password được mã hóa bằng BCrypt
- Kiểm tra trùng lặp username và email
- Role mặc định: ROLE_USER

## Cách sử dụng

### 1. Truy cập trang đăng ký
- Vào `http://localhost:8080/register`
- Hoặc click "Đăng ký ngay" từ trang login

### 2. Điền thông tin
- **Tên đăng nhập**: Nhập tên đăng nhập mong muốn
- **Email**: Nhập email hợp lệ
- **Mật khẩu**: Nhập mật khẩu (tối thiểu 6 ký tự)
- **Xác nhận mật khẩu**: Nhập lại mật khẩu

### 3. Submit form
- Click nút "Đăng ký"
- Hệ thống sẽ kiểm tra và tạo tài khoản
- Nếu thành công, redirect về trang login với thông báo

## Cấu trúc code

### 1. Controller
- `AuthController.java`: Xử lý các request đăng ký

### 2. Service
- `UserRegistrationService.java`: Logic xử lý đăng ký

### 3. Repository
- `UserRepository.java`: Thao tác với database
- `RoleRepository.java`: Quản lý roles
- `UserRoleRepository.java`: Quản lý user-role mapping

### 4. Template
- `register.html`: Giao diện đăng ký

### 5. CSS
- `registration.css`: Style cho trang đăng ký

## Luồng xử lý

1. **User truy cập** `/register`
2. **Hiển thị form** đăng ký
3. **User submit** form với thông tin
4. **Validation** client-side và server-side
5. **Kiểm tra** username/email trùng lặp
6. **Tạo user** mới với password đã mã hóa
7. **Gán role** ROLE_USER mặc định
8. **Redirect** về login với thông báo thành công

## Xử lý lỗi

### 1. Username đã tồn tại
```
"Tên đăng nhập đã tồn tại!"
```

### 2. Email đã được sử dụng
```
"Email đã được sử dụng!"
```

### 3. Password quá ngắn
```
"Mật khẩu phải có ít nhất 6 ký tự!"
```

### 4. Password không khớp
```
"Mật khẩu xác nhận không khớp!"
```

## Test Cases

### 1. Đăng ký thành công
```java
@Test
public void testSuccessfulRegistration() {
    // Test với dữ liệu hợp lệ
}
```

### 2. Username trùng lặp
```java
@Test
public void testRegistrationWithExistingUsername() {
    // Test với username đã tồn tại
}
```

### 3. Email trùng lặp
```java
@Test
public void testRegistrationWithExistingEmail() {
    // Test với email đã tồn tại
}
```

### 4. Password ngắn
```java
@Test
public void testRegistrationWithShortPassword() {
    // Test với password < 6 ký tự
}
```

## Bảo mật

### 1. Password Encryption
- Sử dụng BCrypt để mã hóa password
- Salt tự động được tạo

### 2. Input Validation
- Client-side validation bằng JavaScript
- Server-side validation bằng Java
- SQL injection protection

### 3. CSRF Protection
- Spring Security CSRF protection
- Token validation

## Tùy chỉnh

### 1. Thay đổi validation rules
- Sửa trong `UserRegistrationService.java`
- Thêm validation mới

### 2. Thay đổi giao diện
- Sửa `register.html`
- Cập nhật `registration.css`

### 3. Thêm fields mới
- Cập nhật User model
- Thêm validation
- Cập nhật template

## Troubleshooting

### 1. Lỗi "Role USER not found"
- Chạy lại ứng dụng để khởi tạo roles
- Kiểm tra `DataInitializationService`

### 2. Lỗi database connection
- Kiểm tra cấu hình database
- Kiểm tra SQL Server

### 3. Lỗi validation
- Kiểm tra JavaScript console
- Kiểm tra server logs

## Production Deployment

### 1. Cấu hình database
- Backup database trước khi deploy
- Kiểm tra connection pool

### 2. Security
- Sử dụng HTTPS
- Cấu hình session timeout
- Monitor logs

### 3. Performance
- Cấu hình caching
- Optimize database queries
- Load testing 