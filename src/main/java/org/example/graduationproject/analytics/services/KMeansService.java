package org.example.graduationproject.analytics.services;

import org.example.graduationproject.analytics.models.ClusterResult;
import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.analytics.repositories.CustomerClusterRepository;
import org.example.graduationproject.analytics.algorithms.KMeansAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.example.graduationproject.models.User;

@Service
public class KMeansService {
    
    @Autowired
    private KMeansAlgorithm kMeansAlgorithm;
    
    @Autowired
    private CustomerClusterRepository clusterRepository;
    
    @Autowired
    private RFMAnalysisService rfmAnalysisService;
    
    @Transactional
    public ClusterResult performKMeansClustering(int k) {
        // Lấy dữ liệu RFM mới nhất
        List<RFMData> rfmDataList = rfmAnalysisService.getAllRFMData();
        
        if (rfmDataList.isEmpty()) {
            return new ClusterResult();
        }
        
        // Thực hiện K-means clustering (sẽ gán cluster_id cho normalized data)
        List<CustomerCluster> clusters = kMeansAlgorithm.performKMeans(rfmDataList, k);
        
        // Đặt tên cho các cụm dựa trên đặc điểm
        assignClusterNames(clusters);
        
        // Lưu clusters vào database
        clusterRepository.saveAll(clusters);
        
        // Cập nhật clusterId cho RFM data trong database
        updateRFMDataWithClusters(rfmDataList, clusters);
        
        // Cập nhật lại customer_count cho tất cả clusters
        rfmAnalysisService.refreshAllCustomerCounts();
        
        // Tạo kết quả
        ClusterResult result = new ClusterResult();
        result.setClusters(clusters);
        result.setRfmData(rfmDataList);
        result.setTotalCustomers(rfmDataList.size());
        result.setAnalysisDate(LocalDateTime.now().toString());
        
        return result;
    }
    
    private void assignClusterNames(List<CustomerCluster> clusters) {
        // Sắp xếp clusters theo ID để đảm bảo thứ tự đúng
        clusters.sort((c1, c2) -> c1.getId().compareTo(c2.getId()));
        
        // Map cluster names theo ID - 4 clusters chuẩn RFM
        for (CustomerCluster cluster : clusters) {
            switch (cluster.getId()) {
                case 1:
                    cluster.setClusterName("Champions");
                    cluster.setClusterDescription("Khách hàng VIP - mua hàng thường xuyên, giá trị cao");
                    break;
                case 2:
                    cluster.setClusterName("Loyal Customers");
                    cluster.setClusterDescription("Khách hàng trung thành - mua hàng đều đặn");
                    break;
                case 3:
                    cluster.setClusterName("At Risk");
                    cluster.setClusterDescription("Khách hàng có nguy cơ rời bỏ - cần chăm sóc đặc biệt");
                    break;
                case 4:
                    cluster.setClusterName("Lost");
                    cluster.setClusterDescription("Khách hàng đã mất - cần chiến lược win-back");
                    break;
                default:
                    cluster.setClusterName("Unknown Cluster " + cluster.getId());
                    cluster.setClusterDescription("Cluster không xác định");
                    break;
            }
        }
    }
    
    private void updateRFMDataWithClusters(List<RFMData> rfmDataList, List<CustomerCluster> clusters) {
        // Cập nhật clusterId cho mỗi RFM data trong database
        for (RFMData rfm : rfmDataList) {
            // Lấy cluster_id từ dữ liệu đã được gán bởi K-means algorithm
            Integer clusterId = rfm.getClusterId();
            if (clusterId != null) {
                // Cập nhật cluster_id trực tiếp vào database
                rfmAnalysisService.updateClusterIdForUser(rfm.getUser(), clusterId);
                System.out.println("Updated cluster_id " + clusterId + " for user " + rfm.getUser().getId());
            } else {
                System.out.println("Warning: No cluster_id assigned for user " + rfm.getUser().getId());
            }
        }
    }
    
    /**
     * Cập nhật cluster_id trực tiếp từ mapping user -> cluster_id
     */
    @Transactional
    public void updateClusterIdsFromMapping(Map<Long, Integer> userClusterMapping) {
        for (Map.Entry<Long, Integer> entry : userClusterMapping.entrySet()) {
            Long userId = entry.getKey();
            Integer clusterId = entry.getValue();
            
            // Tạo User object với ID
            User user = new User();
            user.setId(userId);
            
            // Cập nhật cluster_id
            rfmAnalysisService.updateClusterIdForUser(user, clusterId);
            System.out.println("Updated cluster_id " + clusterId + " for user " + userId);
        }
    }
    
    public List<CustomerCluster> getLatestClusters() {
        return clusterRepository.findLatestClusters();
    }
}
