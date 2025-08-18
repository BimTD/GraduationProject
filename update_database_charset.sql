-- Script cập nhật database để hỗ trợ tiếng Việt
-- Chạy script này trên SQL Server Management Studio

-- Cập nhật collation cho database
ALTER DATABASE clothes5 COLLATE Vietnamese_CI_AS;

-- Cập nhật các bảng để sử dụng NVARCHAR cho các trường text
-- Bảng SanPham
ALTER TABLE SanPham ALTER COLUMN ten NVARCHAR(255);
ALTER TABLE SanPham ALTER COLUMN moTa NVARCHAR(MAX);
ALTER TABLE SanPham ALTER COLUMN tag NVARCHAR(255);
ALTER TABLE SanPham ALTER COLUMN huongDan NVARCHAR(MAX);
ALTER TABLE SanPham ALTER COLUMN thanhPhan NVARCHAR(MAX);
ALTER TABLE SanPham ALTER COLUMN trangThaiSanPham NVARCHAR(50);

-- Bảng Loai
ALTER TABLE Loai ALTER COLUMN ten NVARCHAR(255);

-- Bảng NhanHieu
ALTER TABLE NhanHieu ALTER COLUMN ten NVARCHAR(255);

-- Bảng NhaCungCap
ALTER TABLE NhaCungCap ALTER COLUMN ten NVARCHAR(255);
ALTER TABLE NhaCungCap ALTER COLUMN email NVARCHAR(255);
ALTER TABLE NhaCungCap ALTER COLUMN sdt NVARCHAR(20);
ALTER TABLE NhaCungCap ALTER COLUMN thongTin NVARCHAR(MAX);
ALTER TABLE NhaCungCap ALTER COLUMN diaChi NVARCHAR(500);

-- Bảng MauSac
ALTER TABLE MauSac ALTER COLUMN maMau NVARCHAR(100);

-- Bảng Size
ALTER TABLE Size ALTER COLUMN tenSize NVARCHAR(50);

-- Bảng users
ALTER TABLE users ALTER COLUMN username NVARCHAR(255);
ALTER TABLE users ALTER COLUMN password NVARCHAR(255);
ALTER TABLE users ALTER COLUMN enabled NVARCHAR(10);
ALTER TABLE users ALTER COLUMN email NVARCHAR(255);
ALTER TABLE users ALTER COLUMN provider NVARCHAR(50);

-- Bảng PhieuNhapHang
ALTER TABLE PhieuNhapHang ALTER COLUMN soChungTu NVARCHAR(100);
ALTER TABLE PhieuNhapHang ALTER COLUMN nguoiLapPhieu NVARCHAR(255);
ALTER TABLE PhieuNhapHang ALTER COLUMN ghiChu NVARCHAR(MAX);

-- Bảng ImageSanPham
ALTER TABLE ImageSanPham ALTER COLUMN imageName NVARCHAR(255);

-- Bảng role
ALTER TABLE role ALTER COLUMN name NVARCHAR(100);

-- Kiểm tra kết quả
SELECT 'Database đã được cập nhật để hỗ trợ tiếng Việt' as Status;
