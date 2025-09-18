package org.example.graduationproject.analytics.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MarketingTactics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarketingTactic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cluster_id", nullable = false)
    private Integer clusterId; // ID của cluster khách hàng

    @Column(name = "name", length = 100, nullable = false)
    private String name; // Tên chiến thuật (unique identifier)

    @Column(name = "title", length = 255, nullable = false, columnDefinition = "VARCHAR(255)")
    private String title; // Tiêu đề hiển thị

    @Column(name = "description", length = 2000, columnDefinition = "VARCHAR(2000)")
    private String description; // Mô tả chi tiết

    @Column(name = "priority", length = 20, nullable = false)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "category", length = 100, nullable = false, columnDefinition = "VARCHAR(100)")
    private String category; // Loại chiến thuật

    @Column(name = "estimated_impact", length = 50, columnDefinition = "VARCHAR(50)")
    private String estimatedImpact; // High Impact, Medium Impact, Low Impact

    @Column(name = "estimated_cost", length = 50, columnDefinition = "VARCHAR(50)")
    private String estimatedCost; // High Cost, Medium Cost, Low Cost

    @Column(name = "time_to_implement", length = 50, columnDefinition = "VARCHAR(50)")
    private String timeToImplement; // 1-2 weeks, 2-4 weeks, 4-8 weeks

    @Column(name = "budget_required", precision = 18, scale = 2)
    private BigDecimal budgetRequired; // Ngân sách cần thiết

    @Column(name = "expected_roi", precision = 5, scale = 2)
    private BigDecimal expectedROI; // ROI dự kiến (%)

    @Column(name = "status", length = 20, nullable = false)
    private String status = "DRAFT"; // DRAFT, ACTIVE, COMPLETED, CANCELLED

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Có đang hoạt động không

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by", length = 100, columnDefinition = "VARCHAR(100)")
    private String createdBy; // Người tạo

    @Column(name = "notes", length = 1000, columnDefinition = "VARCHAR(1000)")
    private String notes; // Ghi chú thêm

    // Constructor cho việc tạo mới
    public MarketingTactic(Integer clusterId, String name, String title, String description, 
                          String priority, String category, String createdBy) {
        this.clusterId = clusterId;
        this.name = name;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.createdBy = createdBy;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}

