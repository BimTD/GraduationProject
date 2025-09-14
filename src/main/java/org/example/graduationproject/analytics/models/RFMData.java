package org.example.graduationproject.analytics.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.graduationproject.models.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RFMAnalysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RFMData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Integer recencyScore; // Số ngày từ lần mua gần nhất
    private Integer frequencyScore; // Tổng số đơn hàng
    private BigDecimal monetaryScore; // Tổng tiền đã chi
    
    private String rfmScore; // Kết hợp R-F-M (VD: 5-4-3)
    private Integer clusterId; // ID cụm từ K-means (1-4)
    private LocalDateTime analysisDate;
    
    // Các trường chuẩn hóa (không lưu vào DB)
    @Transient
    private Double normalizedRecency;
    @Transient
    private Double normalizedFrequency;
    @Transient
    private Double normalizedMonetary;
    
    public RFMData(User user, Integer recency, Integer frequency, BigDecimal monetary) {
        this.user = user;
        this.recencyScore = recency;
        this.frequencyScore = frequency;
        this.monetaryScore = monetary;
        this.analysisDate = LocalDateTime.now();
    }
}
