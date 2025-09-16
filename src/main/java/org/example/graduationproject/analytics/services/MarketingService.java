package org.example.graduationproject.analytics.services;

import org.example.graduationproject.analytics.models.CustomerCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketingService {
    
    @Autowired
    private CustomerSegmentationService segmentationService;
    
    @Autowired
    private RFMAnalysisService rfmAnalysisService;
    
    // Đề xuất chiến thuật marketing cho từng cluster
    public Map<String, Object> getMarketingStrategies() {
        Map<String, Object> result = new HashMap<>();
        
        // Lấy danh sách clusters
        List<CustomerCluster> clusters = segmentationService.getCustomerSegments();
        
        // Tạo chiến thuật cho từng cluster
        List<Map<String, Object>> strategies = clusters.stream()
            .map(this::createMarketingStrategy)
            .collect(Collectors.toList());
        
        result.put("strategies", strategies);
        result.put("totalClusters", clusters.size());
        result.put("lastUpdated", new Date());
        
        return result;
    }
    
    // Tạo chiến thuật cho một cluster cụ thể
    private Map<String, Object> createMarketingStrategy(CustomerCluster cluster) {
        Map<String, Object> strategy = new HashMap<>();
        
        strategy.put("clusterId", cluster.getId());
        strategy.put("clusterName", cluster.getClusterName());
        strategy.put("clusterDescription", cluster.getClusterDescription());
        strategy.put("customerCount", cluster.getCustomerCount());
        
        // Đặc trưng cluster
        Map<String, Object> characteristics = new HashMap<>();
        characteristics.put("recencyAvg", cluster.getRecencyAvg());
        characteristics.put("frequencyAvg", cluster.getFrequencyAvg());
        characteristics.put("monetaryAvg", cluster.getMonetaryAvg());
        strategy.put("characteristics", characteristics);
        
        // Chiến thuật marketing
        List<Map<String, Object>> marketingTactics = getMarketingTactics(cluster);
        strategy.put("marketingTactics", marketingTactics);
        
        // Mục tiêu và KPIs
        Map<String, Object> objectives = getMarketingObjectives(cluster);
        strategy.put("objectives", objectives);
        
        // Ưu tiên thực hiện
        strategy.put("priority", getPriority(cluster));
        
        return strategy;
    }
    
    // Lấy chiến thuật marketing cụ thể cho từng cluster
    private List<Map<String, Object>> getMarketingTactics(CustomerCluster cluster) {
        List<Map<String, Object>> tactics = new ArrayList<>();
        
        switch (cluster.getClusterName()) {
            case "Champions":
                tactics.addAll(getChampionsTactics());
                break;
            case "Loyal Customers":
                tactics.addAll(getLoyalCustomersTactics());
                break;
            case "At Risk":
                tactics.addAll(getAtRiskTactics());
                break;
            case "Lost":
                tactics.addAll(getLostTactics());
                break;
            default:
                tactics.addAll(getDefaultTactics());
        }
        
        return tactics;
    }
    
    // Chiến thuật cho Champions (VIP)
    private List<Map<String, Object>> getChampionsTactics() {
        List<Map<String, Object>> tactics = new ArrayList<>();
        
        tactics.add(createTactic(
            "VIP Program",
            "Tạo chương trình VIP đặc biệt",
            "Tặng thẻ VIP, ưu đãi độc quyền, sản phẩm mới trước",
            "HIGH",
            "Loyalty Program"
        ));
        
        tactics.add(createTactic(
            "Premium Products",
            "Giới thiệu sản phẩm cao cấp",
            "Ưu tiên giới thiệu sản phẩm mới, phiên bản giới hạn",
            "HIGH",
            "Product Marketing"
        ));
        
        tactics.add(createTactic(
            "Personal Service",
            "Dịch vụ cá nhân hóa",
            "Tư vấn riêng, giao hàng ưu tiên, hỗ trợ 24/7",
            "MEDIUM",
            "Customer Service"
        ));
        
        tactics.add(createTactic(
            "Referral Program",
            "Chương trình giới thiệu",
            "Tặng thưởng khi giới thiệu khách hàng mới",
            "MEDIUM",
            "Referral Marketing"
        ));
        
        return tactics;
    }
    
    // Chiến thuật cho Loyal Customers
    private List<Map<String, Object>> getLoyalCustomersTactics() {
        List<Map<String, Object>> tactics = new ArrayList<>();
        
        tactics.add(createTactic(
            "Loyalty Rewards",
            "Chương trình tích điểm",
            "Tích điểm cho mỗi đơn hàng, đổi quà tặng",
            "HIGH",
            "Loyalty Program"
        ));
        
        tactics.add(createTactic(
            "Cross-selling",
            "Bán chéo sản phẩm",
            "Gợi ý sản phẩm bổ sung, combo tiết kiệm",
            "HIGH",
            "Cross-selling"
        ));
        
        tactics.add(createTactic(
            "Seasonal Campaigns",
            "Chiến dịch theo mùa",
            "Khuyến mãi đặc biệt theo mùa, ngày lễ",
            "MEDIUM",
            "Seasonal Marketing"
        ));
        
        tactics.add(createTactic(
            "Email Marketing",
            "Email marketing cá nhân hóa",
            "Gửi email với sản phẩm phù hợp, khuyến mãi",
            "MEDIUM",
            "Email Marketing"
        ));
        
        return tactics;
    }
    
    // Chiến thuật cho At Risk
    private List<Map<String, Object>> getAtRiskTactics() {
        List<Map<String, Object>> tactics = new ArrayList<>();
        
        tactics.add(createTactic(
            "Win-back Campaign",
            "Chiến dịch win-back",
            "Khuyến mãi mạnh, ưu đãi đặc biệt để thu hút lại",
            "HIGH",
            "Retention Marketing"
        ));
        
        tactics.add(createTactic(
            "Personal Outreach",
            "Liên hệ trực tiếp",
            "Gọi điện, SMS cá nhân để tìm hiểu lý do",
            "HIGH",
            "Direct Marketing"
        ));
        
        tactics.add(createTactic(
            "Survey & Feedback",
            "Khảo sát và phản hồi",
            "Gửi khảo sát để hiểu nhu cầu, cải thiện dịch vụ",
            "MEDIUM",
            "Customer Research"
        ));
        
        tactics.add(createTactic(
            "Limited Time Offers",
            "Ưu đãi có thời hạn",
            "Khuyến mãi giới hạn thời gian để tạo cảm giác cấp bách",
            "MEDIUM",
            "Urgency Marketing"
        ));
        
        return tactics;
    }
    
    // Chiến thuật cho Lost
    private List<Map<String, Object>> getLostTactics() {
        List<Map<String, Object>> tactics = new ArrayList<>();
        
        tactics.add(createTactic(
            "Reactivation Campaign",
            "Chiến dịch kích hoạt lại",
            "Khuyến mãi mạnh nhất, ưu đãi đặc biệt",
            "HIGH",
            "Reactivation Marketing"
        ));
        
        tactics.add(createTactic(
            "New Product Launch",
            "Ra mắt sản phẩm mới",
            "Thông báo sản phẩm mới, ưu đãi đặc biệt",
            "MEDIUM",
            "Product Launch"
        ));
        
        tactics.add(createTactic(
            "Exit Survey",
            "Khảo sát lý do rời bỏ",
            "Tìm hiểu lý do để cải thiện dịch vụ",
            "LOW",
            "Customer Research"
        ));
        
        tactics.add(createTactic(
            "Long-term Nurturing",
            "Nuôi dưỡng dài hạn",
            "Gửi nội dung giá trị, không bán hàng",
            "LOW",
            "Content Marketing"
        ));
        
        return tactics;
    }
    
    // Chiến thuật mặc định
    private List<Map<String, Object>> getDefaultTactics() {
        List<Map<String, Object>> tactics = new ArrayList<>();
        
        tactics.add(createTactic(
            "General Marketing",
            "Marketing tổng quát",
            "Áp dụng chiến thuật marketing chung",
            "MEDIUM",
            "General Marketing"
        ));
        
        return tactics;
    }
    
    // Tạo một chiến thuật
    private Map<String, Object> createTactic(String name, String title, String description, String priority, String category) {
        Map<String, Object> tactic = new HashMap<>();
        tactic.put("name", name);
        tactic.put("title", title);
        tactic.put("description", description);
        tactic.put("priority", priority);
        tactic.put("category", category);
        tactic.put("estimatedImpact", getEstimatedImpact(priority));
        tactic.put("estimatedCost", getEstimatedCost(priority));
        tactic.put("timeToImplement", getTimeToImplement(priority));
        return tactic;
    }
    
    // Lấy mục tiêu marketing cho cluster
    private Map<String, Object> getMarketingObjectives(CustomerCluster cluster) {
        Map<String, Object> objectives = new HashMap<>();
        
        switch (cluster.getClusterName()) {
            case "Champions":
                objectives.put("primary", "Tăng giá trị đơn hàng và tần suất mua");
                objectives.put("secondary", "Tăng tỷ lệ giới thiệu và retention");
                objectives.put("kpi", "Tăng 20% AOV, 15% frequency");
                break;
            case "Loyal Customers":
                objectives.put("primary", "Duy trì loyalty và tăng cross-selling");
                objectives.put("secondary", "Chuyển đổi lên Champions");
                objectives.put("kpi", "Tăng 10% AOV, 5% frequency");
                break;
            case "At Risk":
                objectives.put("primary", "Ngăn chặn churn và win-back");
                objectives.put("secondary", "Khôi phục tần suất mua hàng");
                objectives.put("kpi", "Giảm 50% churn rate, tăng 30% reactivation");
                break;
            case "Lost":
                objectives.put("primary", "Kích hoạt lại và thu hút về");
                objectives.put("secondary", "Tìm hiểu lý do rời bỏ");
                objectives.put("kpi", "Tăng 20% reactivation rate");
                break;
            default:
                objectives.put("primary", "Tăng engagement và conversion");
                objectives.put("secondary", "Cải thiện customer experience");
                objectives.put("kpi", "Tăng 10% engagement rate");
        }
        
        return objectives;
    }
    
    // Lấy độ ưu tiên thực hiện
    private String getPriority(CustomerCluster cluster) {
        switch (cluster.getClusterName()) {
            case "Champions":
                return "HIGH"; // Ưu tiên cao vì đây là khách hàng VIP
            case "At Risk":
                return "HIGH"; // Ưu tiên cao vì cần ngăn chặn churn
            case "Loyal Customers":
                return "MEDIUM"; // Ưu tiên trung bình
            case "Lost":
                return "LOW"; // Ưu tiên thấp vì khó thu hút lại
            default:
                return "MEDIUM";
        }
    }
    
    // Ước tính tác động
    private String getEstimatedImpact(String priority) {
        switch (priority) {
            case "HIGH":
                return "High Impact";
            case "MEDIUM":
                return "Medium Impact";
            case "LOW":
                return "Low Impact";
            default:
                return "Unknown";
        }
    }
    
    // Ước tính chi phí
    private String getEstimatedCost(String priority) {
        switch (priority) {
            case "HIGH":
                return "High Cost";
            case "MEDIUM":
                return "Medium Cost";
            case "LOW":
                return "Low Cost";
            default:
                return "Unknown";
        }
    }
    
    // Thời gian triển khai
    private String getTimeToImplement(String priority) {
        switch (priority) {
            case "HIGH":
                return "1-2 weeks";
            case "MEDIUM":
                return "2-4 weeks";
            case "LOW":
                return "4-8 weeks";
            default:
                return "Unknown";
        }
    }
    
    // Lấy chiến thuật cho một cluster cụ thể
    public Map<String, Object> getMarketingStrategyForCluster(Integer clusterId) {
        List<CustomerCluster> clusters = segmentationService.getCustomerSegments();
        CustomerCluster cluster = clusters.stream()
            .filter(c -> c.getId().equals(clusterId))
            .findFirst()
            .orElse(null);
        
        if (cluster == null) {
            return null;
        }
        
        return createMarketingStrategy(cluster);
    }
    
    // Lấy thống kê tổng quan
    public Map<String, Object> getMarketingOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<CustomerCluster> clusters = segmentationService.getCustomerSegments();
        
        int totalCustomers = clusters.stream()
            .mapToInt(CustomerCluster::getCustomerCount)
            .sum();
        
        overview.put("totalCustomers", totalCustomers);
        overview.put("totalClusters", clusters.size());
        overview.put("clusters", clusters);
        
        // Tính tỷ lệ phần trăm
        Map<String, Double> percentages = new HashMap<>();
        for (CustomerCluster cluster : clusters) {
            double percentage = (double) cluster.getCustomerCount() / totalCustomers * 100;
            percentages.put(cluster.getClusterName(), percentage);
        }
        overview.put("percentages", percentages);
        
        return overview;
    }
}








