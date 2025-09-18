package org.example.graduationproject.analytics.repositories;

import org.example.graduationproject.analytics.models.MarketingObjective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketingObjectiveRepository extends JpaRepository<MarketingObjective, Long> {
    
    // Tìm objectives theo cluster ID và active status
    List<MarketingObjective> findByClusterIdAndIsActiveTrueOrderByPriorityAscCreatedDateDesc(Integer clusterId);
    
    // Tìm tất cả objectives theo cluster ID (bao gồm cả inactive)
    List<MarketingObjective> findByClusterIdOrderByPriorityAscCreatedDateDesc(Integer clusterId);
    
    // Tìm objective theo cluster ID và primary objective (để check duplicate)
    Optional<MarketingObjective> findByClusterIdAndPrimaryObjective(Integer clusterId, String primaryObjective);
    
    // Đếm số objectives theo cluster ID
    long countByClusterId(Integer clusterId);
    
    // Đếm số objectives theo cluster ID và status
    long countByClusterIdAndStatus(Integer clusterId, String status);
    
    // Đếm số objectives theo cluster ID và priority
    long countByClusterIdAndPriority(Integer clusterId, String priority);
    
    // Lấy thống kê objectives theo priority
    @Query("SELECT o.priority, COUNT(o) FROM MarketingObjective o WHERE o.clusterId = :clusterId GROUP BY o.priority")
    List<Object[]> getObjectiveCountByPriority(@Param("clusterId") Integer clusterId);
    
    // Lấy thống kê objectives theo status
    @Query("SELECT o.status, COUNT(o) FROM MarketingObjective o WHERE o.clusterId = :clusterId GROUP BY o.status")
    List<Object[]> getObjectiveCountByStatus(@Param("clusterId") Integer clusterId);
}
