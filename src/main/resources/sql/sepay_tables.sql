-- SQL Scripts for SePay Integration
-- Run these scripts in your SQL Server database

-- 1. Create sepay_transactions table
CREATE TABLE sepay_transactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transaction_id NVARCHAR(100) UNIQUE NOT NULL,
    order_id NVARCHAR(50),
    amount DECIMAL(18,2) NOT NULL,
    description NVARCHAR(500),
    bank_account NVARCHAR(50),
    bank_name NVARCHAR(100),
    qr_code_url NVARCHAR(500),
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    webhook_data NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    completed_at DATETIME2,
    user_id BIGINT,
    hoa_don_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (hoa_don_id) REFERENCES HoaDon(id)
);

-- Create indexes for better performance
CREATE INDEX IX_sepay_transactions_transaction_id ON sepay_transactions(transaction_id);
CREATE INDEX IX_sepay_transactions_order_id ON sepay_transactions(order_id);
CREATE INDEX IX_sepay_transactions_user_id ON sepay_transactions(user_id);
CREATE INDEX IX_sepay_transactions_status ON sepay_transactions(status);
CREATE INDEX IX_sepay_transactions_created_at ON sepay_transactions(created_at);

-- 2. Create view for easy querying
CREATE VIEW v_SePayTransactions AS
SELECT 
    s.id,
    s.transaction_id,
    s.order_id,
    s.amount,
    s.description,
    s.bank_account,
    s.bank_name,
    s.qr_code_url,
    s.status,
    s.created_at,
    s.updated_at,
    s.completed_at,
    u.username,
    u.email,
    u.ho_ten,
    h.ngay_tao as order_created_at,
    h.trang_thai as order_status
FROM sepay_transactions s
LEFT JOIN users u ON s.user_id = u.id
LEFT JOIN HoaDon h ON s.hoa_don_id = h.id;

PRINT 'SePay tables created successfully!';
