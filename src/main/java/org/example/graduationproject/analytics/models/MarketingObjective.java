package org.example.graduationproject.analytics.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_objectives")
public class MarketingObjective {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cluster_id", nullable = false)
    private Integer clusterId;
    
    @Column(name = "primary_objective", nullable = false, length = 500)
    private String primaryObjective;
    
    @Column(name = "secondary_objective", length = 500)
    private String secondaryObjective;
    
    @Column(name = "kpi", length = 200)
    private String kpi;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "target_value")
    private String targetValue;
    
    @Column(name = "measurement_period", length = 100)
    private String measurementPeriod;
    
    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM";
    
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    // Constructors
    public MarketingObjective() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public MarketingObjective(Integer clusterId, String primaryObjective, String secondaryObjective, String kpi) {
        this();
        this.clusterId = clusterId;
        this.primaryObjective = primaryObjective;
        this.secondaryObjective = secondaryObjective;
        this.kpi = kpi;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getClusterId() {
        return clusterId;
    }
    
    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
    
    public String getPrimaryObjective() {
        return primaryObjective;
    }
    
    public void setPrimaryObjective(String primaryObjective) {
        this.primaryObjective = primaryObjective;
    }
    
    public String getSecondaryObjective() {
        return secondaryObjective;
    }
    
    public void setSecondaryObjective(String secondaryObjective) {
        this.secondaryObjective = secondaryObjective;
    }
    
    public String getKpi() {
        return kpi;
    }
    
    public void setKpi(String kpi) {
        this.kpi = kpi;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTargetValue() {
        return targetValue;
    }
    
    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }
    
    public String getMeasurementPeriod() {
        return measurementPeriod;
    }
    
    public void setMeasurementPeriod(String measurementPeriod) {
        this.measurementPeriod = measurementPeriod;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
