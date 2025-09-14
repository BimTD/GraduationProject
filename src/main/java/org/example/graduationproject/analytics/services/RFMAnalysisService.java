package org.example.graduationproject.analytics.services;

import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.repositories.RFMAnalysisRepository;
import org.example.graduationproject.analytics.repositories.CustomerClusterRepository;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.HoaDonRepository;
import org.example.graduationproject.analytics.algorithms.RFMCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RFMAnalysisService {
    
    @Autowired
    private RFMAnalysisRepository rfmAnalysisRepository;
    
    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private RFMCalculator rfmCalculator;
    
    @Autowired
    private CustomerClusterRepository clusterRepository;
    
    @Transactional
    public void calculateRFMForAllCustomers() {
        // Lấy tất cả user có đơn hàng
        List<User> usersWithOrders = hoaDonRepository.findAll().stream()
            .map(HoaDon::getUser)
            .distinct()
            .collect(Collectors.toList());
        
        for (User user : usersWithOrders) {
            List<HoaDon> userOrders = hoaDonRepository.findByUserOrderByNgayTaoDesc(user);
            RFMData rfmData = rfmCalculator.calculateRFM(user, userOrders);
            
            // Lưu hoặc cập nhật RFM data
            RFMData existingRFM = rfmAnalysisRepository.findByUser(user).stream()
                .findFirst().orElse(null);
            
            if (existingRFM != null) {
                existingRFM.setRecencyScore(rfmData.getRecencyScore());
                existingRFM.setFrequencyScore(rfmData.getFrequencyScore());
                existingRFM.setMonetaryScore(rfmData.getMonetaryScore());
                existingRFM.setRfmScore(rfmData.getRfmScore());
                existingRFM.setAnalysisDate(rfmData.getAnalysisDate());
                rfmAnalysisRepository.save(existingRFM);
            } else {
                rfmAnalysisRepository.save(rfmData);
            }
        }
    }
    
    public List<RFMData> getAllRFMData() {
        return rfmAnalysisRepository.findLatestAnalysis();
    }
    
    public List<RFMData> getRFMDataByCluster(Integer clusterId) {
        return rfmAnalysisRepository.findLatestAnalysisByCluster(clusterId);
    }
    
    public RFMData getRFMDataForUser(User user) {
        return rfmAnalysisRepository.findByUser(user).stream()
            .findFirst().orElse(null);
    }
    
    @Transactional
    public void calculateRFMForUser(User user) {
        List<HoaDon> userOrders = hoaDonRepository.findByUserOrderByNgayTaoDesc(user);
        if (userOrders.isEmpty()) {
            return;
        }
        
        RFMData rfmData = rfmCalculator.calculateRFM(user, userOrders);
        
        // Lưu hoặc cập nhật RFM data
        RFMData existingRFM = rfmAnalysisRepository.findByUser(user).stream()
            .findFirst().orElse(null);
        
        if (existingRFM != null) {
            existingRFM.setRecencyScore(rfmData.getRecencyScore());
            existingRFM.setFrequencyScore(rfmData.getFrequencyScore());
            existingRFM.setMonetaryScore(rfmData.getMonetaryScore());
            existingRFM.setRfmScore(rfmData.getRfmScore());
            existingRFM.setAnalysisDate(rfmData.getAnalysisDate());
            rfmAnalysisRepository.save(existingRFM);
        } else {
            rfmAnalysisRepository.save(rfmData);
        }
        
        // Tự động gán cluster_id cho user mới hoặc cập nhật cluster_id cho user hiện có
        try {
            assignSimpleClusterToUser(user);
        } catch (Exception e) {
            System.err.println("Error assigning cluster to user " + user.getId() + ": " + e.getMessage());
        }
    }
    
    @Transactional
    public void saveRFMData(RFMData rfmData) {
        rfmAnalysisRepository.save(rfmData);
    }
    
    @Transactional
    public void updateClusterIdForUser(User user, Integer clusterId) {
        RFMData rfmData = rfmAnalysisRepository.findByUser(user).stream()
            .findFirst().orElse(null);
        
        if (rfmData != null) {
            rfmData.setClusterId(clusterId);
            rfmAnalysisRepository.save(rfmData);
            System.out.println("Updated cluster_id " + clusterId + " for user " + user.getId());
        }
    }
    
    /**
     * Tự động gán cluster_id cho user dựa trên RFM data hiện tại
     */
    @Transactional
    public void assignClusterToUser(User user) {
        // Lấy RFM data của user
        RFMData rfmData = rfmAnalysisRepository.findByUser(user).stream()
            .findFirst().orElse(null);
        
        if (rfmData == null) {
            System.out.println("No RFM data found for user " + user.getId());
            return;
        }
        
        // Lấy clusters hiện tại
        List<CustomerCluster> clusters = clusterRepository.findLatestClusters();
        if (clusters.isEmpty()) {
            System.out.println("No clusters found, cannot assign cluster to user " + user.getId());
            return;
        }
        
        // Tìm cluster phù hợp nhất dựa trên RFM scores
        Integer bestClusterId = findBestClusterForUser(rfmData, clusters);
        
        if (bestClusterId != null) {
            rfmData.setClusterId(bestClusterId);
            rfmAnalysisRepository.save(rfmData);
            System.out.println("Assigned cluster_id " + bestClusterId + " to user " + user.getId());
        } else {
            System.out.println("Could not assign cluster to user " + user.getId());
        }
    }
    
    /**
     * Gán cluster đơn giản dựa trên RFM scores (không cần KMeansService)
     */
    @Transactional
    public void assignSimpleClusterToUser(User user) {
        // Lấy RFM data của user
        RFMData rfmData = rfmAnalysisRepository.findByUser(user).stream()
            .findFirst().orElse(null);
        
        if (rfmData == null) {
            System.out.println("No RFM data found for user " + user.getId());
            return;
        }
        
        // Gán cluster đơn giản dựa trên RFM scores
        Integer clusterId = assignClusterBasedOnRFM(rfmData);
        
        if (clusterId != null) {
            // Lưu cluster_id cũ để cập nhật customer_count
            Integer oldClusterId = rfmData.getClusterId();
            
            rfmData.setClusterId(clusterId);
            rfmAnalysisRepository.save(rfmData);
            
            // Cập nhật customer_count cho clusters
            updateCustomerCountForClusters(oldClusterId, clusterId);
            
            System.out.println("Assigned simple cluster_id " + clusterId + " to user " + user.getId());
        } else {
            System.out.println("Could not assign simple cluster to user " + user.getId());
        }
    }
    
    /**
     * Tìm cluster phù hợp nhất cho user dựa trên RFM scores
     */
    private Integer findBestClusterForUser(RFMData rfmData, List<CustomerCluster> clusters) {
        if (clusters.isEmpty()) {
            return null;
        }
        
        // Tính khoảng cách đến mỗi cluster centroid
        double minDistance = Double.MAX_VALUE;
        Integer bestClusterId = null;
        
        for (CustomerCluster cluster : clusters) {
            // Tính khoảng cách Euclidean giữa RFM scores của user và cluster centroid
            double distance = calculateDistanceToCluster(rfmData, cluster);
            
            if (distance < minDistance) {
                minDistance = distance;
                bestClusterId = cluster.getId();
            }
        }
        
        return bestClusterId;
    }
    
    /**
     * Tính khoảng cách Euclidean giữa RFM data và cluster centroid
     */
    private double calculateDistanceToCluster(RFMData rfmData, CustomerCluster cluster) {
        // Normalize RFM scores để so sánh với cluster centroids
        double rScore = normalizeScore(rfmData.getRecencyScore(), 1, 5);
        double fScore = normalizeScore(rfmData.getFrequencyScore(), 1, 5);
        double mScore = normalizeMonetaryScore(rfmData.getMonetaryScore());
        
        // Tính khoảng cách Euclidean
        double rDiff = rScore - cluster.getNormalizedRecency();
        double fDiff = fScore - cluster.getNormalizedFrequency();
        double mDiff = mScore - cluster.getNormalizedMonetary();
        
        return Math.sqrt(rDiff * rDiff + fDiff * fDiff + mDiff * mDiff);
    }
    
    /**
     * Normalize score từ 1-5 thành 0-1
     */
    private double normalizeScore(Integer score, int min, int max) {
        if (score == null) return 0.0;
        return (double) (score - min) / (max - min);
    }
    
    /**
     * Normalize monetary score thành 0-1 (giả sử max là 10M VNĐ)
     */
    private double normalizeMonetaryScore(BigDecimal monetary) {
        if (monetary == null) return 0.0;
        double maxMonetary = 10000000.0; // 10M VNĐ
        return Math.min(monetary.doubleValue() / maxMonetary, 1.0);
    }
    
    /**
     * Gán cluster đơn giản dựa trên RFM scores (5 cụm khách hàng)
     * Sử dụng raw values để tính RFM Scores (1-5) rồi gán cluster
     */
    private Integer assignClusterBasedOnRFM(RFMData rfmData) {
        Integer recency = rfmData.getRecencyScore(); // Số ngày thực tế
        Integer frequency = rfmData.getFrequencyScore(); // Số lần mua thực tế
        BigDecimal monetary = rfmData.getMonetaryScore(); // Số tiền thực tế
        
        if (recency == null || frequency == null || monetary == null) {
            return null;
        }
        
        // Chuyển đổi raw values thành RFM Scores (1-5)
        int recencyScore = calculateRecencyScore(recency);
        int frequencyScore = calculateFrequencyScore(frequency);
        int monetaryScore = calculateMonetaryScore(monetary);
        
        // Logic gán cluster dựa trên RFM Scores (1-5) - 4 clusters chuẩn RFM
        // 1 - Champions: R=4-5, F=4-5, M=4-5 (khách hàng VIP)
        if (recencyScore >= 4 && frequencyScore >= 4 && monetaryScore >= 4) {
            return 1; // Champions
        }
        // 2 - Loyal Customers: R=3-5, F=3-5, M=3-5 (khách hàng trung thành)
        else if (recencyScore >= 3 && frequencyScore >= 3 && monetaryScore >= 3) {
            return 2; // Loyal Customers
        }
        // 3 - At Risk: R=1-2, F≥2, M≥2 (có nguy cơ rời bỏ)
        else if (recencyScore <= 2 && frequencyScore >= 2 && monetaryScore >= 2) {
            return 3; // At Risk
        }
        // 4 - Lost: Tất cả trường hợp còn lại
        else {
            return 4; // Lost
        }
    }
    
    /**
     * Tính Recency Score (1-5) từ số ngày thực tế
     */
    private int calculateRecencyScore(int recency) {
        if (recency <= 30) return 5;
        if (recency <= 60) return 4;
        if (recency <= 90) return 3;
        if (recency <= 180) return 2;
        return 1;
    }
    
    /**
     * Tính Frequency Score (1-5) từ số lần mua thực tế
     */
    private int calculateFrequencyScore(int frequency) {
        if (frequency >= 20) return 5;
        if (frequency >= 10) return 4;
        if (frequency >= 5) return 3;
        if (frequency >= 2) return 2;
        return 1;
    }
    
    /**
     * Tính Monetary Score (1-5) từ số tiền thực tế
     */
    private int calculateMonetaryScore(BigDecimal monetary) {
        if (monetary.compareTo(new BigDecimal("5000000")) >= 0) return 5;
        if (monetary.compareTo(new BigDecimal("2000000")) >= 0) return 4;
        if (monetary.compareTo(new BigDecimal("1000000")) >= 0) return 3;
        if (monetary.compareTo(new BigDecimal("500000")) >= 0) return 2;
        return 1;
    }
    
    /**
     * Cập nhật customer_count cho clusters khi cluster_id thay đổi
     */
    @Transactional
    public void updateCustomerCountForClusters(Integer oldClusterId, Integer newClusterId) {
        try {
            // Giảm customer_count của cluster cũ (nếu có)
            if (oldClusterId != null) {
                CustomerCluster oldCluster = clusterRepository.findById(oldClusterId).orElse(null);
                if (oldCluster != null) {
                    int currentCount = oldCluster.getCustomerCount() != null ? oldCluster.getCustomerCount() : 0;
                    oldCluster.setCustomerCount(Math.max(0, currentCount - 1));
                    clusterRepository.save(oldCluster);
                    System.out.println("Decreased customer_count for cluster " + oldClusterId + " to " + oldCluster.getCustomerCount());
                }
            }
            
            // Tăng customer_count của cluster mới
            if (newClusterId != null) {
                CustomerCluster newCluster = clusterRepository.findById(newClusterId).orElse(null);
                if (newCluster != null) {
                    int currentCount = newCluster.getCustomerCount() != null ? newCluster.getCustomerCount() : 0;
                    newCluster.setCustomerCount(currentCount + 1);
                    clusterRepository.save(newCluster);
                    System.out.println("Increased customer_count for cluster " + newClusterId + " to " + newCluster.getCustomerCount());
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating customer_count for clusters: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật lại customer_count cho tất cả clusters dựa trên dữ liệu thực tế
     */
    @Transactional
    public void refreshAllCustomerCounts() {
        try {
            // Lấy tất cả clusters
            List<CustomerCluster> allClusters = clusterRepository.findAll();
            
            for (CustomerCluster cluster : allClusters) {
                // Đếm số khách hàng thực tế trong cluster này
                long actualCount = rfmAnalysisRepository.countByClusterId(cluster.getId());
                
                // Cập nhật customer_count
                cluster.setCustomerCount((int) actualCount);
                clusterRepository.save(cluster);
                
                System.out.println("Updated customer_count for cluster " + cluster.getId() + " (" + cluster.getClusterName() + ") to " + actualCount);
            }
            
            System.out.println("Successfully refreshed customer_count for all clusters");
        } catch (Exception e) {
            System.err.println("Error refreshing customer counts: " + e.getMessage());
        }
    }
}
