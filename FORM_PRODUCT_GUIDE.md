# Hướng dẫn sử dụng Form Sản phẩm

## Tổng quan
Form sản phẩm đã được hoàn thiện với đầy đủ các chức năng:
- Thông tin cơ bản sản phẩm
- Upload ảnh sản phẩm
- Chọn loại, nhãn hiệu, nhà cung cấp

## Các trường thông tin

### Thông tin cơ bản:
- **Name**: Tên sản phẩm (bắt buộc)
- **Selling Price**: Giá bán (bắt buộc)
- **Import Price**: Giá nhập (bắt buộc)
- **Promotion**: Khuyến mãi (tùy chọn)
- **Gender**: Giới tính (bắt buộc)
  - Nam (1)
  - Nữ (2)
  - Phi giới tính (3)

### Thông tin mô tả:
- **Description**: Mô tả sản phẩm
- **Tag**: Tags sản phẩm
- **Guide**: Hướng dẫn sử dụng
- **Ingredient**: Thành phần

### Thông tin phân loại:
- **Category**: Loại sản phẩm (bắt buộc)
- **Brand**: Nhãn hiệu (bắt buộc)
- **Supplier**: Nhà cung cấp (bắt buộc)

### Upload ảnh:
- Hỗ trợ drag & drop hoặc click để chọn
- Có thể upload nhiều ảnh cùng lúc
- Preview ảnh trước khi upload
- Có thể xóa ảnh đã chọn

## Cách sử dụng

1. **Truy cập form**: `/admin/productForm`
2. **Điền thông tin**: Điền đầy đủ các trường bắt buộc
3. **Upload ảnh**: 
   - Kéo thả ảnh vào vùng upload hoặc
   - Click "Choose Images" để chọn file
4. **Lưu sản phẩm**: Click "Save Product"

## Lưu ý kỹ thuật

### Upload ảnh:
- Ảnh được lưu trong thư mục: `src/main/resources/static/uploads/products/`
- Tên file được tạo unique bằng UUID
- Hỗ trợ các định dạng: jpg, jpeg, png, gif, webp

### Database:
- Sản phẩm được lưu vào bảng `SanPham`
- Ảnh được lưu vào bảng `ImageSanPham`
- Quan hệ 1-n giữa sản phẩm và ảnh

### Validation:
- Các trường bắt buộc được validate ở frontend
- Xử lý lỗi và hiển thị thông báo phù hợp

## Cấu trúc file

```
src/main/resources/templates/admin/form-product.html  # Form template
src/main/java/org/example/graduationproject/controllers/admin/ProductController.java  # Controller
src/main/java/org/example/graduationproject/services/SanPhamService.java  # Service interface
src/main/java/org/example/graduationproject/services/impl/SanPhamServiceImpl.java  # Service implementation
src/main/java/org/example/graduationproject/repositories/ImageSanPhamRepository.java  # Image repository
src/main/resources/static/uploads/products/  # Thư mục lưu ảnh
```

## Tính năng nâng cao

- **Drag & Drop**: Hỗ trợ kéo thả ảnh
- **Preview**: Xem trước ảnh trước khi upload
- **Multiple Upload**: Upload nhiều ảnh cùng lúc
- **Remove Image**: Xóa ảnh đã chọn
- **Responsive**: Giao diện responsive cho mobile 