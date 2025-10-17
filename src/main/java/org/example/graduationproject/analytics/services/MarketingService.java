package org.example.graduationproject.analytics.services;

import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.models.MarketingTactic;
import org.example.graduationproject.analytics.models.MarketingObjective;
import org.example.graduationproject.analytics.repositories.MarketingTacticRepository;
import org.example.graduationproject.analytics.repositories.MarketingObjectiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketingService {
    
    @Autowired
    private CustomerSegmentationService segmentationService;
    
    @Autowired
    private RFMAnalysisService rfmAnalysisService;
    
    @Autowired
    private MarketingTacticRepository marketingTacticRepository;
    
    @Autowired
    private MarketingObjectiveRepository marketingObjectiveRepository;
    
    @Autowired
    private MarketingReportService marketingReportService;
    
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
        
        // Chiến thuật marketing từ database
        List<Map<String, Object>> marketingTactics = getMarketingTacticsFromDatabase(cluster.getId());
        strategy.put("marketingTactics", marketingTactics);
        
        // Mục tiêu và KPIs
        Map<String, Object> objectives = getMarketingObjectives(cluster);
        strategy.put("objectives", objectives);
        
        // Marketing Objectives từ database
        List<Map<String, Object>> marketingObjectives = getMarketingObjectivesFromDatabase(cluster.getId());
        strategy.put("marketingObjectives", marketingObjectives);
        
        // Ưu tiên thực hiện
        strategy.put("priority", getPriority(cluster));
        
        return strategy;
    }
    
    // Lấy chiến thuật marketing từ database
    private List<Map<String, Object>> getMarketingTacticsFromDatabase(Integer clusterId) {
        List<MarketingTactic> tactics = marketingTacticRepository.findByClusterIdOrderByPriorityAscCreatedDateDesc(clusterId);
        
        // Trả về danh sách chiến thuật từ database (có thể rỗng)
        return tactics.stream()
                .map(this::convertTacticToMap)
                .collect(Collectors.toList());
    }
    
    // Lấy mục tiêu marketing từ database
    private List<Map<String, Object>> getMarketingObjectivesFromDatabase(Integer clusterId) {
        List<MarketingObjective> objectives = marketingObjectiveRepository.findByClusterIdOrderByPriorityAscCreatedDateDesc(clusterId);
        
        // Trả về danh sách mục tiêu từ database (có thể rỗng)
        return objectives.stream()
                .map(this::convertObjectiveToMap)
                .collect(Collectors.toList());
    }
    
    
    // Chuyển đổi MarketingTactic thành Map
    private Map<String, Object> convertTacticToMap(MarketingTactic tactic) {
        Map<String, Object> tacticMap = new HashMap<>();
        tacticMap.put("id", tactic.getId());
        tacticMap.put("name", tactic.getName());
        tacticMap.put("title", tactic.getTitle());
        tacticMap.put("description", tactic.getDescription());
        tacticMap.put("priority", tactic.getPriority());
        tacticMap.put("category", tactic.getCategory());
        tacticMap.put("estimatedImpact", tactic.getEstimatedImpact());
        tacticMap.put("estimatedCost", tactic.getEstimatedCost());
        tacticMap.put("timeToImplement", tactic.getTimeToImplement());
        tacticMap.put("budgetRequired", tactic.getBudgetRequired());
        tacticMap.put("expectedROI", tactic.getExpectedROI());
        tacticMap.put("status", tactic.getStatus());
        tacticMap.put("isActive", tactic.getIsActive());
        tacticMap.put("createdDate", tactic.getCreatedDate());
        tacticMap.put("updatedDate", tactic.getUpdatedDate());
        tacticMap.put("createdBy", tactic.getCreatedBy());
        tacticMap.put("notes", tactic.getNotes());
        return tacticMap;
    }
    
    
    // Lấy mục tiêu marketing cho cluster từ database
    private Map<String, Object> getMarketingObjectives(CustomerCluster cluster) {
        List<MarketingObjective> objectives = marketingObjectiveRepository.findByClusterIdAndIsActiveTrueOrderByPriorityAscCreatedDateDesc(cluster.getId());
        
        if (objectives.isEmpty()) {
            // Trả về null nếu chưa có mục tiêu trong database
            return null;
        }
        
        // Lấy mục tiêu đầu tiên (có thể có nhiều mục tiêu nhưng hiển thị cái chính)
        MarketingObjective mainObjective = objectives.get(0);
        
        Map<String, Object> objectiveMap = new HashMap<>();
        objectiveMap.put("id", mainObjective.getId());
        objectiveMap.put("primary", mainObjective.getPrimaryObjective());
        objectiveMap.put("secondary", mainObjective.getSecondaryObjective());
        objectiveMap.put("kpi", mainObjective.getKpi());
        objectiveMap.put("description", mainObjective.getDescription());
        objectiveMap.put("targetValue", mainObjective.getTargetValue());
        objectiveMap.put("measurementPeriod", mainObjective.getMeasurementPeriod());
        objectiveMap.put("priority", mainObjective.getPriority());
        objectiveMap.put("status", mainObjective.getStatus());
        objectiveMap.put("isActive", mainObjective.getIsActive());
        objectiveMap.put("createdDate", mainObjective.getCreatedDate());
        objectiveMap.put("updatedDate", mainObjective.getUpdatedDate());
        objectiveMap.put("createdBy", mainObjective.getCreatedBy());
        objectiveMap.put("notes", mainObjective.getNotes());
        
        return objectiveMap;
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
    
    // ========== CRUD METHODS FOR MARKETING TACTICS ==========
    
    // Lấy tất cả chiến thuật của một cluster
    public List<MarketingTactic> getTacticsByClusterId(Integer clusterId) {
        return marketingTacticRepository.findByClusterIdOrderByPriorityAscCreatedDateDesc(clusterId);
    }
    
    // Lấy chiến thuật theo ID
    public MarketingTactic getTacticById(Integer tacticId) {
        return marketingTacticRepository.findById(tacticId).orElse(null);
    }
    
    // Tạo chiến thuật mới
    public MarketingTactic createTactic(MarketingTactic tactic) {
        // Kiểm tra tên chiến thuật đã tồn tại chưa
        MarketingTactic existingTactic = marketingTacticRepository.findByNameAndClusterId(tactic.getName(), tactic.getClusterId());
        if (existingTactic != null) {
            throw new RuntimeException("Tên chiến thuật đã tồn tại trong cluster này");
        }
        
        tactic.setCreatedDate(java.time.LocalDateTime.now());
        tactic.setUpdatedDate(java.time.LocalDateTime.now());
        return marketingTacticRepository.save(tactic);
    }
    
    // Cập nhật chiến thuật
    public MarketingTactic updateTactic(MarketingTactic tactic) {
        MarketingTactic existingTactic = marketingTacticRepository.findById(tactic.getId()).orElse(null);
        if (existingTactic == null) {
            throw new RuntimeException("Không tìm thấy chiến thuật");
        }
        
        // Kiểm tra tên chiến thuật đã tồn tại chưa (trừ chính nó)
        MarketingTactic duplicateTactic = marketingTacticRepository.findByNameAndClusterId(tactic.getName(), tactic.getClusterId());
        if (duplicateTactic != null && !duplicateTactic.getId().equals(tactic.getId())) {
            throw new RuntimeException("Tên chiến thuật đã tồn tại trong cluster này");
        }
        
        tactic.setUpdatedDate(java.time.LocalDateTime.now());
        return marketingTacticRepository.save(tactic);
    }
    
    // Xóa chiến thuật (xóa vĩnh viễn khỏi database)
    public void deleteTactic(Integer tacticId) {
        if (!marketingTacticRepository.existsById(tacticId)) {
            throw new RuntimeException("Không tìm thấy chiến thuật");
        }
        marketingTacticRepository.deleteById(tacticId);
    }
    
    
    // Kích hoạt/vô hiệu hóa chiến thuật
    public MarketingTactic toggleTacticStatus(Integer tacticId) {
        MarketingTactic tactic = marketingTacticRepository.findById(tacticId).orElse(null);
        if (tactic == null) {
            throw new RuntimeException("Không tìm thấy chiến thuật");
        }
        
        tactic.setIsActive(!tactic.getIsActive());
        tactic.setUpdatedDate(java.time.LocalDateTime.now());
        return marketingTacticRepository.save(tactic);
    }
    
    // Cập nhật trạng thái chiến thuật
    public MarketingTactic updateTacticStatus(Integer tacticId, String status) {
        MarketingTactic tactic = marketingTacticRepository.findById(tacticId).orElse(null);
        if (tactic == null) {
            throw new RuntimeException("Không tìm thấy chiến thuật");
        }
        
        tactic.setStatus(status);
        tactic.setUpdatedDate(java.time.LocalDateTime.now());
        return marketingTacticRepository.save(tactic);
    }
    
    // Lấy thống kê chiến thuật theo cluster
    public Map<String, Object> getTacticStatistics(Integer clusterId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> priorityStats = marketingTacticRepository.getTacticCountByPriority(clusterId);
        List<Object[]> categoryStats = marketingTacticRepository.getTacticCountByCategory(clusterId);
        
        Map<String, Long> priorityCount = new HashMap<>();
        for (Object[] stat : priorityStats) {
            priorityCount.put((String) stat[0], (Long) stat[1]);
        }
        
        Map<String, Long> categoryCount = new HashMap<>();
        for (Object[] stat : categoryStats) {
            categoryCount.put((String) stat[0], (Long) stat[1]);
        }
        
        stats.put("totalTactics", marketingTacticRepository.countByClusterId(clusterId));
        stats.put("activeTactics", marketingTacticRepository.countByClusterIdAndStatus(clusterId, "ACTIVE"));
        stats.put("priorityCount", priorityCount);
        stats.put("categoryCount", categoryCount);
        
        return stats;
    }
    
    // ========== MARKETING OBJECTIVES CRUD METHODS ==========
    
    // Lấy tất cả objectives theo cluster ID
    public List<Map<String, Object>> getObjectivesByClusterId(Integer clusterId) {
        List<MarketingObjective> objectives = marketingObjectiveRepository.findByClusterIdOrderByPriorityAscCreatedDateDesc(clusterId);
        return objectives.stream()
                .map(this::convertObjectiveToMap)
                .collect(Collectors.toList());
    }
    
    // Lấy objective theo ID
    public Map<String, Object> getObjectiveById(Long objectiveId) {
        MarketingObjective objective = marketingObjectiveRepository.findById(objectiveId).orElse(null);
        if (objective == null) {
            throw new RuntimeException("Không tìm thấy mục tiêu marketing");
        }
        return convertObjectiveToMap(objective);
    }
    
    // Tạo objective mới
    public Map<String, Object> createObjective(Map<String, Object> objectiveData) {
        MarketingObjective objective = new MarketingObjective();
        
        // Xử lý clusterId có thể là String hoặc Integer
        Object clusterIdObj = objectiveData.get("clusterId");
        Integer clusterId;
        if (clusterIdObj instanceof String) {
            clusterId = Integer.parseInt((String) clusterIdObj);
        } else {
            clusterId = (Integer) clusterIdObj;
        }
        objective.setClusterId(clusterId);
        objective.setPrimaryObjective((String) objectiveData.get("primaryObjective"));
        objective.setSecondaryObjective((String) objectiveData.get("secondaryObjective"));
        objective.setKpi((String) objectiveData.get("kpi"));
        objective.setDescription((String) objectiveData.get("description"));
        objective.setTargetValue((String) objectiveData.get("targetValue"));
        objective.setMeasurementPeriod((String) objectiveData.get("measurementPeriod"));
        objective.setPriority((String) objectiveData.getOrDefault("priority", "MEDIUM"));
        objective.setStatus((String) objectiveData.getOrDefault("status", "ACTIVE"));
        objective.setIsActive((Boolean) objectiveData.getOrDefault("isActive", true));
        objective.setCreatedBy((String) objectiveData.getOrDefault("createdBy", "Admin"));
        objective.setNotes((String) objectiveData.get("notes"));
        
        objective.setCreatedDate(java.time.LocalDateTime.now());
        objective.setUpdatedDate(java.time.LocalDateTime.now());
        
        MarketingObjective savedObjective = marketingObjectiveRepository.save(objective);
        return convertObjectiveToMap(savedObjective);
    }
    
    // Cập nhật objective
    public Map<String, Object> updateObjective(Long objectiveId, Map<String, Object> objectiveData) {
        MarketingObjective objective = marketingObjectiveRepository.findById(objectiveId).orElse(null);
        if (objective == null) {
            throw new RuntimeException("Không tìm thấy mục tiêu marketing");
        }
        
        if (objectiveData.containsKey("primaryObjective")) {
            objective.setPrimaryObjective((String) objectiveData.get("primaryObjective"));
        }
        if (objectiveData.containsKey("secondaryObjective")) {
            objective.setSecondaryObjective((String) objectiveData.get("secondaryObjective"));
        }
        if (objectiveData.containsKey("kpi")) {
            objective.setKpi((String) objectiveData.get("kpi"));
        }
        if (objectiveData.containsKey("description")) {
            objective.setDescription((String) objectiveData.get("description"));
        }
        if (objectiveData.containsKey("targetValue")) {
            objective.setTargetValue((String) objectiveData.get("targetValue"));
        }
        if (objectiveData.containsKey("measurementPeriod")) {
            objective.setMeasurementPeriod((String) objectiveData.get("measurementPeriod"));
        }
        if (objectiveData.containsKey("priority")) {
            objective.setPriority((String) objectiveData.get("priority"));
        }
        if (objectiveData.containsKey("status")) {
            objective.setStatus((String) objectiveData.get("status"));
        }
        if (objectiveData.containsKey("notes")) {
            objective.setNotes((String) objectiveData.get("notes"));
        }
        
        objective.setUpdatedDate(java.time.LocalDateTime.now());
        
        MarketingObjective savedObjective = marketingObjectiveRepository.save(objective);
        return convertObjectiveToMap(savedObjective);
    }
    
    // Xóa objective (permanent delete)
    public void deleteObjective(Long objectiveId) {
        if (!marketingObjectiveRepository.existsById(objectiveId)) {
            throw new RuntimeException("Không tìm thấy mục tiêu marketing");
        }
        marketingObjectiveRepository.deleteById(objectiveId);
    }
    
    // Toggle trạng thái active của objective
    public Map<String, Object> toggleObjectiveStatus(Long objectiveId) {
        MarketingObjective objective = marketingObjectiveRepository.findById(objectiveId).orElse(null);
        if (objective == null) {
            throw new RuntimeException("Không tìm thấy mục tiêu marketing");
        }
        
        objective.setIsActive(!objective.getIsActive());
        objective.setUpdatedDate(java.time.LocalDateTime.now());
        
        MarketingObjective savedObjective = marketingObjectiveRepository.save(objective);
        return convertObjectiveToMap(savedObjective);
    }
    
    // Cập nhật status của objective
    public Map<String, Object> updateObjectiveStatus(Long objectiveId, String status) {
        MarketingObjective objective = marketingObjectiveRepository.findById(objectiveId).orElse(null);
        if (objective == null) {
            throw new RuntimeException("Không tìm thấy mục tiêu marketing");
        }
        
        objective.setStatus(status);
        objective.setUpdatedDate(java.time.LocalDateTime.now());
        
        MarketingObjective savedObjective = marketingObjectiveRepository.save(objective);
        return convertObjectiveToMap(savedObjective);
    }
    
    // Chuyển đổi MarketingObjective thành Map
    private Map<String, Object> convertObjectiveToMap(MarketingObjective objective) {
        Map<String, Object> objectiveMap = new HashMap<>();
        objectiveMap.put("id", objective.getId());
        objectiveMap.put("clusterId", objective.getClusterId());
        objectiveMap.put("primaryObjective", objective.getPrimaryObjective());
        objectiveMap.put("secondaryObjective", objective.getSecondaryObjective());
        objectiveMap.put("kpi", objective.getKpi());
        objectiveMap.put("description", objective.getDescription());
        objectiveMap.put("targetValue", objective.getTargetValue());
        objectiveMap.put("measurementPeriod", objective.getMeasurementPeriod());
        objectiveMap.put("priority", objective.getPriority());
        objectiveMap.put("status", objective.getStatus());
        objectiveMap.put("isActive", objective.getIsActive());
        objectiveMap.put("createdDate", objective.getCreatedDate());
        objectiveMap.put("updatedDate", objective.getUpdatedDate());
        objectiveMap.put("createdBy", objective.getCreatedBy());
        objectiveMap.put("notes", objective.getNotes());
        return objectiveMap;
    }
    
    // ========== REPORT EXPORT METHODS ==========
    
    // Xuất báo cáo PDF
    public byte[] exportMarketingReportToPdf(Integer clusterId) throws IOException {
        return marketingReportService.exportMarketingReportToPdf(clusterId);
    }
    
    // Xuất báo cáo Excel
    public byte[] exportMarketingReportToExcel(Integer clusterId) throws IOException {
        return marketingReportService.exportMarketingReportToExcel(clusterId);
    }
}
















