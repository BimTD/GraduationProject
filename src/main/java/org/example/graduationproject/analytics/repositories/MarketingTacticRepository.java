package org.example.graduationproject.analytics.repositories;

import org.example.graduationproject.analytics.models.MarketingTactic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingTacticRepository extends JpaRepository<MarketingTactic, Integer> {
    
    // Tìm tất cả chiến thuật theo cluster ID
    List<MarketingTactic> findByClusterIdOrderByPriorityAscCreatedDateDesc(Integer clusterId);
    
    // Tìm chiến thuật theo cluster ID và trạng thái
    List<MarketingTactic> findByClusterIdAndStatusOrderByPriorityAscCreatedDateDesc(Integer clusterId, String status);
    
    // Tìm chiến thuật theo cluster ID và ưu tiên
    List<MarketingTactic> findByClusterIdAndPriorityOrderByCreatedDateDesc(Integer clusterId, String priority);
    
    // Tìm chiến thuật đang hoạt động theo cluster ID
    List<MarketingTactic> findByClusterIdAndIsActiveTrueOrderByPriorityAscCreatedDateDesc(Integer clusterId);
    
    // Tìm chiến thuật theo tên và cluster ID (để check duplicate)
    MarketingTactic findByNameAndClusterId(String name, Integer clusterId);
    
    // Đếm số chiến thuật theo cluster ID
    long countByClusterId(Integer clusterId);
    
    // Đếm số chiến thuật theo cluster ID và trạng thái
    long countByClusterIdAndStatus(Integer clusterId, String status);
    
    // Tìm chiến thuật theo category
    List<MarketingTactic> findByCategoryOrderByPriorityAscCreatedDateDesc(String category);
    
    // Tìm chiến thuật theo priority
    List<MarketingTactic> findByPriorityOrderByCreatedDateDesc(String priority);
    
    // Tìm tất cả chiến thuật đang hoạt động
    List<MarketingTactic> findByIsActiveTrueOrderByClusterIdAscPriorityAscCreatedDateDesc();
    
    // Query tùy chỉnh để lấy thống kê theo cluster
    @Query("SELECT m.priority, COUNT(m) FROM MarketingTactic m WHERE m.clusterId = :clusterId GROUP BY m.priority")
    List<Object[]> getTacticCountByPriority(@Param("clusterId") Integer clusterId);
    
    // Query để lấy thống kê theo category
    @Query("SELECT m.category, COUNT(m) FROM MarketingTactic m WHERE m.clusterId = :clusterId GROUP BY m.category")
    List<Object[]> getTacticCountByCategory(@Param("clusterId") Integer clusterId);
}

