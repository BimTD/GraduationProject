package org.example.graduationproject.analytics.repositories;

import org.example.graduationproject.analytics.models.CustomerCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerClusterRepository extends JpaRepository<CustomerCluster, Integer> {
    
    @Query("SELECT c FROM CustomerCluster c WHERE c.createdDate = (SELECT MAX(c2.createdDate) FROM CustomerCluster c2)")
    List<CustomerCluster> findLatestClusters();
}
