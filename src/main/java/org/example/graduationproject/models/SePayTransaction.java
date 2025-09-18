package org.example.graduationproject.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sepay_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SePayTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, columnDefinition = "VARCHAR(100)")
    private String transactionId; // ID giao dịch từ SePay

    @Column(name = "order_id", columnDefinition = "VARCHAR(50)")
    private String orderId; // ID đơn hàng trong hệ thống

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount; // Số tiền giao dịch

    @Column(name = "description", columnDefinition = "VARCHAR(500)")
    private String description; // Mô tả giao dịch

    @Column(name = "bank_account", columnDefinition = "VARCHAR(50)")
    private String bankAccount; // Số tài khoản ngân hàng

    @Column(name = "bank_name", columnDefinition = "VARCHAR(100)")
    private String bankName; // Tên ngân hàng

    @Column(name = "qr_code_url", columnDefinition = "VARCHAR(500)")
    private String qrCodeUrl; // URL QR code

    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private String status; // PENDING, SUCCESS, FAILED, CANCELLED

    @Column(name = "webhook_data", columnDefinition = "TEXT")
    private String webhookData; // Dữ liệu webhook từ SePay

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "hoa_don_id")
    private HoaDon hoaDon;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
