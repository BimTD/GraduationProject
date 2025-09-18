package org.example.graduationproject.analytics.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerClusters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cluster_name", length = 100, nullable = false)
    private String clusterName; // "Champions", "Loyal Customers", "At Risk", "Lost"

    @Column(name = "cluster_description", length = 1000, columnDefinition = "VARCHAR(1000)")
    private String clusterDescription;

    @Column(name = "recency_avg", precision = 10, scale = 2)
    private BigDecimal recencyAvg;

    @Column(name = "frequency_avg", precision = 10, scale = 2)
    private BigDecimal frequencyAvg;

    @Column(name = "monetary_avg", precision = 18, scale = 2)
    private BigDecimal monetaryAvg;

    @Column(name = "customer_count")
    private Integer customerCount;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    // Các trường chuẩn hóa cho thuật toán
    @Transient
    private Double normalizedRecency;
    @Transient
    private Double normalizedFrequency;
    @Transient
    private Double normalizedMonetary;

    public CustomerCluster(String clusterName, String description) {
        this.clusterName = clusterName;
        this.clusterDescription = description;
        this.createdDate = LocalDateTime.now();
    }
}























