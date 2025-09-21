-- Thêm cột email_sent và email_sent_at vào bảng GioHang
ALTER TABLE GioHang ADD COLUMN email_sent BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE GioHang ADD COLUMN email_sent_at TIMESTAMP NULL;

-- Cập nhật tất cả giỏ hàng hiện tại có email_sent = false
UPDATE GioHang SET email_sent = FALSE WHERE email_sent IS NULL;
