package org.example.graduationproject.analytics.services;

import org.example.graduationproject.analytics.models.ClusterResult;
import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerSegmentationService {

    @Autowired
    private KMeansService kMeansService;

    @Autowired
    private RFMAnalysisService rfmAnalysisService;

    public ClusterResult performCustomerSegmentation() {
        return kMeansService.performKMeansClustering(4); // 4 cụm khách hàng chuẩn RFM
    }

    public List<CustomerCluster> getCustomerSegments() {
        return kMeansService.getLatestClusters();
    }

    public CustomerCluster getCustomerSegment(User user) {
        RFMData rfmData = rfmAnalysisService.getRFMDataForUser(user);
        if (rfmData == null || rfmData.getClusterId() == null) {
            return null;
        }

        List<CustomerCluster> clusters = kMeansService.getLatestClusters();
        return clusters.stream()
                .filter(cluster -> cluster.getId().equals(rfmData.getClusterId()))
                .findFirst()
                .orElse(null);
    }

    public List<RFMData> getCustomersInSegment(Integer clusterId) {
        return rfmAnalysisService.getRFMDataByCluster(clusterId);
    }

    public String getCustomerRecommendations(User user) {
        CustomerCluster segment = getCustomerSegment(user);
        if (segment == null) {
            return "Không có dữ liệu phân tích cho khách hàng này.";
        }

        switch (segment.getClusterName()) {
            case "Champions":
                return "Khách hàng VIP - Ưu tiên chăm sóc, cung cấp sản phẩm cao cấp, chương trình ưu đãi đặc biệt";
            case "Loyal Customers":
                return "Khách hàng trung thành - Duy trì mối quan hệ, cung cấp sản phẩm phù hợp, chương trình khuyến mãi";
            case "At Risk":
                return "Khách hàng có nguy cơ rời bỏ - Cần chăm sóc đặc biệt, khuyến mãi hấp dẫn, liên hệ trực tiếp";
            case "Lost":
                return "Khách hàng đã mất - Chiến lược win-back, khuyến mãi mạnh, khảo sát lý do rời bỏ";
            default:
                return "Không có khuyến nghị cụ thể.";
        }
    }
}