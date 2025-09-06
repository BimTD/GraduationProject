# Hướng dẫn sửa lỗi Search cho Anonymous Users

## Vấn đề:
Khi chưa đăng nhập, chức năng tìm kiếm không hoạt động.

## Nguyên nhân:
Trong `SecurityConfig.java`, các endpoint `/api/search/**` và `/api/categories/**` không được cấu hình `permitAll()`, nên chúng bị block bởi `.anyRequest().authenticated()`.

## Giải pháp đã áp dụng:

### 1. **Cập nhật SecurityConfig.java**

**File:** `src/main/java/org/example/graduationproject/config/SecurityConfig.java`

**Trước khi sửa:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/assets/**","/fe/**").permitAll()
    .requestMatchers("/", "/home", "/login", "/register", "/oauth2/**", "/oauth2/login", "/oauth2/success").permitAll()
    .requestMatchers("/product-details/**").permitAll()
    .requestMatchers("/about/**").permitAll()
    .requestMatchers("/contact/**").permitAll()
    .requestMatchers("/blog/**").permitAll()
    .requestMatchers("/shop/**").permitAll()
    .requestMatchers("/api/products/**").permitAll()
    .requestMatchers("/api/cart/**").authenticated()
    .requestMatchers("/cart").authenticated()
    .requestMatchers("/checkout/**").authenticated()
    .requestMatchers("/orders/**").authenticated()
    .requestMatchers("/profile/**").authenticated()
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasRole("USER")
    .anyRequest().authenticated()
)
```

**Sau khi sửa:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/assets/**","/fe/**").permitAll()
    .requestMatchers("/", "/home", "/login", "/register", "/oauth2/**", "/oauth2/login", "/oauth2/success").permitAll()
    .requestMatchers("/product-details/**").permitAll()
    .requestMatchers("/about/**").permitAll()
    .requestMatchers("/contact/**").permitAll()
    .requestMatchers("/blog/**").permitAll()
    .requestMatchers("/shop/**").permitAll()
    .requestMatchers("/api/products/**").permitAll()
    .requestMatchers("/api/search/**").permitAll()  // ✅ Thêm dòng này
    .requestMatchers("/api/categories/**").permitAll()  // ✅ Thêm dòng này
    .requestMatchers("/api/cart/**").authenticated()
    .requestMatchers("/cart").authenticated()
    .requestMatchers("/checkout/**").authenticated()
    .requestMatchers("/orders/**").authenticated()
    .requestMatchers("/profile/**").authenticated()
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasRole("USER")
    .anyRequest().authenticated()
)
```

### 2. **Các API endpoints được mở:**

- ✅ `/api/search/suggestions` - Tìm kiếm gợi ý sản phẩm
- ✅ `/api/search/quick` - Tìm kiếm nhanh
- ✅ `/api/search/popular` - Tìm kiếm phổ biến
- ✅ `/api/search/history` - Lịch sử tìm kiếm
- ✅ `/api/categories` - Danh sách danh mục

## Lý do tại sao cần mở cho anonymous users:

### 1. **UX tốt hơn:**
- User chưa đăng nhập vẫn có thể tìm kiếm sản phẩm
- Khuyến khích user khám phá website
- Tăng conversion rate

### 2. **Business logic:**
- Tìm kiếm sản phẩm là chức năng cơ bản
- Không cần authentication để xem sản phẩm
- Chỉ cần authentication khi mua hàng

### 3. **Security:**
- Search API chỉ đọc dữ liệu, không thay đổi
- Không có thông tin nhạy cảm
- An toàn để mở cho public

## Cách test:

### 1. **Khởi động ứng dụng:**
```bash
mvn spring-boot:run
```

### 2. **Test với file test:**
- Mở `test_search_anonymous.html` trong trình duyệt
- Test các chức năng tìm kiếm

### 3. **Test trực tiếp:**
```bash
# Test search suggestions
curl "http://localhost:8080/api/search/suggestions?q=áo&limit=5"

# Test quick search
curl "http://localhost:8080/api/search/quick?q=quần&limit=5"

# Test popular searches
curl "http://localhost:8080/api/search/popular?limit=10"

# Test categories
curl "http://localhost:8080/api/categories"
```

### 4. **Test trong browser:**
1. Mở `http://localhost:8080` (chưa đăng nhập)
2. Gõ từ khóa vào ô tìm kiếm
3. Kiểm tra có gợi ý hiện ra không
4. Click vào sản phẩm để xem chi tiết

## Kết quả sau khi sửa:

### ✅ **Trước khi sửa:**
```
❌ Search không hoạt động khi chưa đăng nhập
❌ API trả về 401 Unauthorized
❌ User không thể tìm kiếm sản phẩm
❌ UX kém, user phải đăng nhập trước
```

### ✅ **Sau khi sửa:**
```
✅ Search hoạt động cho anonymous users
✅ API trả về kết quả tìm kiếm
✅ User có thể tìm kiếm và xem sản phẩm
✅ UX tốt, user có thể khám phá trước khi đăng nhập
```

## Các API endpoints được mở:

### 1. **Search Suggestions API:**
```
GET /api/search/suggestions?q={query}&limit={limit}
```
- **Mục đích:** Gợi ý sản phẩm khi user gõ
- **Parameters:** 
  - `q`: Từ khóa tìm kiếm
  - `limit`: Số lượng kết quả (default: 8)
- **Response:** Danh sách sản phẩm gợi ý

### 2. **Quick Search API:**
```
GET /api/search/quick?q={query}&categoryId={id}&limit={limit}
```
- **Mục đích:** Tìm kiếm nhanh sản phẩm
- **Parameters:**
  - `q`: Từ khóa tìm kiếm
  - `categoryId`: ID danh mục (optional)
  - `limit`: Số lượng kết quả (default: 20)
- **Response:** Trang sản phẩm tìm được

### 3. **Popular Searches API:**
```
GET /api/search/popular?limit={limit}
```
- **Mục đích:** Lấy danh sách từ khóa tìm kiếm phổ biến
- **Parameters:**
  - `limit`: Số lượng từ khóa (default: 10)
- **Response:** Danh sách từ khóa phổ biến

### 4. **Categories API:**
```
GET /api/categories
```
- **Mục đích:** Lấy danh sách danh mục sản phẩm
- **Response:** Danh sách danh mục

## Lưu ý bảo mật:

### 1. **Chỉ đọc dữ liệu:**
- Các API này chỉ đọc dữ liệu
- Không thay đổi database
- Không truy cập thông tin nhạy cảm

### 2. **Rate limiting:**
- Có thể thêm rate limiting nếu cần
- Tránh spam requests
- Bảo vệ server

### 3. **Caching:**
- Có thể cache kết quả tìm kiếm
- Giảm tải database
- Tăng performance

## Troubleshooting:

### Nếu vẫn không hoạt động:
1. **Restart application** - Security config cần restart
2. **Kiểm tra browser cache** - Clear cache và reload
3. **Kiểm tra console** - Xem có lỗi JavaScript không
4. **Kiểm tra network tab** - Xem API calls có thành công không

### Nếu có lỗi 403 Forbidden:
1. **Kiểm tra SecurityConfig** - Đảm bảo có `.permitAll()`
2. **Kiểm tra URL pattern** - Đảm bảo match đúng
3. **Kiểm tra order** - `.permitAll()` phải trước `.authenticated()`

## Files đã cập nhật:

- ✅ `SecurityConfig.java` - Thêm permitAll cho search và categories API
- ✅ `test_search_anonymous.html` - File test chức năng
- ✅ `SEARCH_ANONYMOUS_FIX.md` - Hướng dẫn này

## Kết luận:

Lỗi search cho anonymous users đã được sửa bằng cách:
1. **Cấu hình Security** để cho phép truy cập search API
2. **Mở các endpoint** cần thiết cho anonymous users
3. **Test thoroughly** để đảm bảo hoạt động

Bây giờ user chưa đăng nhập vẫn có thể tìm kiếm sản phẩm! 🎉
