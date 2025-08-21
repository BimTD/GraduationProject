# Hướng dẫn sửa cú pháp fragment cũ cho tất cả file admin

## Các file cần sửa:

### 1. bulk-product-variant.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Tạo Biến Thể Sản Phẩm Hàng Loạt')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Tạo Biến Thể Sản Phẩm Hàng Loạt')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 2. brand.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Brand Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Brand Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 3. form-product-variant.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Product Variant Form')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Product Variant Form')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 4. form-product.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Product Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Product Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 5. color.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Color Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Color Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 6. category.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Category Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Category Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 7. import-detail.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Chi tiết phiếu nhập')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Chi tiết phiếu nhập')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 8. import-form.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Tạo phiếu nhập hàng')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Tạo phiếu nhập hàng')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
```

### 9. supplier.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Supplier Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Supplier Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 10. size.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Size Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Size Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 11. import.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Import Product Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Import Product Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 12. product.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Product Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Product Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

### 13. product-variant.html
```html
<!-- Thay thế tất cả: -->
th:replace="admin/fragments/head :: head('Product Variant Management')"
th:replace="admin/fragments/sidebar :: sidebar"
th:replace="admin/fragments/header :: header"
th:replace="admin/fragments/script :: script"

<!-- Thành: -->
th:replace="~{admin/fragments/head :: head('Product Variant Management')}"
th:replace="~{admin/fragments/sidebar :: sidebar}"
th:replace="~{admin/fragments/header :: header}"
th:replace="~{admin/fragments/script :: script}"
```

## Cách sửa nhanh:

### Sử dụng Find & Replace trong editor:
1. **Find**: `th:replace="admin/fragments/`
2. **Replace**: `th:replace="~{admin/fragments/`
3. **Find**: `th:replace="user/fragments/`
4. **Replace**: `th:replace="~{user/fragments/`

### Hoặc sử dụng regex:
1. **Find**: `th:replace="([^"]*fragments[^"]*)"`
2. **Replace**: `th:replace="~{$1}"`

## Lưu ý:
- Đảm bảo thêm `~{` vào đầu và `}` vào cuối
- Kiểm tra cú pháp sau khi sửa
- Restart ứng dụng để áp dụng thay đổi
