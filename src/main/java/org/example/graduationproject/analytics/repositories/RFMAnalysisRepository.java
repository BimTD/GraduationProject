package org.example.graduationproject.analytics.repositories;

import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RFMAnalysisRepository extends JpaRepository<RFMData, Long> {

    List<RFMData> findByClusterId(Integer clusterId);

    List<RFMData> findByUser(User user);

    @Query("SELECT r FROM RFMData r WHERE r.analysisDate = (SELECT MAX(r2.analysisDate) FROM RFMData r2)")
    List<RFMData> findLatestAnalysis();

    @Query("SELECT r FROM RFMData r WHERE r.clusterId = :clusterId AND r.analysisDate = (SELECT MAX(r2.analysisDate) FROM RFMData r2)")
    List<RFMData> findLatestAnalysisByCluster(Integer clusterId);

    long countByClusterId(Integer clusterId);
}