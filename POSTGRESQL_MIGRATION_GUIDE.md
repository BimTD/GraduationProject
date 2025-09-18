# Hướng dẫn Migration từ SQL Server sang PostgreSQL

## Tổng quan
Dự án đã được chuyển đổi từ SQL Server sang PostgreSQL. Tất cả các thay đổi cần thiết đã được thực hiện.

## Các thay đổi đã thực hiện

### 1. Dependencies (pom.xml)
- ✅ Đã có PostgreSQL driver: `org.postgresql:postgresql:42.7.7`
- ✅ Không cần thay đổi gì thêm

### 2. Application Properties
- ✅ Đã cấu hình đúng:
  - `spring.datasource.driver-class-name=org.postgresql.Driver`
  - `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`
  - Các cấu hình encoding UTF-8 đã phù hợp

### 3. Entity Models
Đã thay đổi tất cả các column definition từ SQL Server sang PostgreSQL:

#### Thay đổi kiểu dữ liệu:
- `NVARCHAR(n)` → `VARCHAR(n)`
- `NVARCHAR(MAX)` → `TEXT`
- `BIT` → `BOOLEAN`

#### Các file đã được cập nhật:
- ✅ User.java
- ✅ SanPham.java
- ✅ HoaDon.java
- ✅ MaGiamGia.java
- ✅ ChatMessage.java
- ✅ SePayTransaction.java
- ✅ GioHang.java
- ✅ ImageSanPham.java
- ✅ Notification.java
- ✅ MauSac.java
- ✅ Size.java
- ✅ NhaCungCap.java
- ✅ Loai.java
- ✅ NhanHieu.java
- ✅ PhieuNhapHang.java
- ✅ Role.java

### 4. Queries
- ✅ Tất cả queries đều sử dụng JPQL, không có native SQL
- ✅ Không cần thay đổi gì

### 5. Các annotation JPA
- ✅ Tất cả đều sử dụng `@GeneratedValue(strategy = GenerationType.IDENTITY)` - phù hợp với PostgreSQL
- ✅ Không cần thay đổi gì

## Cấu hình Database

### Biến môi trường cần thiết:
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### Ví dụ cấu hình cho PostgreSQL:
```properties
# Local PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/graduation_project
spring.datasource.username=postgres
spring.datasource.password=your_password

# Hoặc sử dụng biến môi trường
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## Các bước triển khai

1. **Cài đặt PostgreSQL** (nếu chưa có)
2. **Tạo database mới** cho dự án
3. **Cấu hình biến môi trường** với thông tin database
4. **Chạy ứng dụng** - Hibernate sẽ tự động tạo tables

## Lưu ý quan trọng

### 1. Migration dữ liệu
Nếu bạn có dữ liệu từ SQL Server cần migrate:
- Sử dụng công cụ migration như pgloader hoặc viết script custom
- Kiểm tra encoding UTF-8 cho dữ liệu tiếng Việt

### 2. Performance
- PostgreSQL có thể có performance khác với SQL Server
- Có thể cần điều chỉnh các index và query

### 3. Backup
- Luôn backup dữ liệu trước khi migration
- Test kỹ trên môi trường development trước

## Kiểm tra sau migration

1. **Chạy ứng dụng** và kiểm tra logs
2. **Test các chức năng chính**:
   - Đăng nhập/đăng ký
   - Quản lý sản phẩm
   - Đặt hàng
   - Thanh toán
3. **Kiểm tra dữ liệu** được lưu đúng format
4. **Test performance** với dữ liệu thực tế

## Troubleshooting

### Lỗi thường gặp:
1. **Connection refused**: Kiểm tra PostgreSQL service và port
2. **Authentication failed**: Kiểm tra username/password
3. **Database not found**: Tạo database trước khi chạy
4. **Encoding issues**: Đảm bảo database sử dụng UTF-8

### Logs để kiểm tra:
- Application logs: `spring.jpa.show-sql=true` đã được bật
- PostgreSQL logs: Kiểm tra trong `/var/log/postgresql/` (Linux) hoặc Event Viewer (Windows)
