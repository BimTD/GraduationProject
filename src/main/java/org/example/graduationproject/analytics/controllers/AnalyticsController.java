package org.example.graduationproject.analytics.controllers;

import org.example.graduationproject.analytics.models.ClusterResult;
import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.analytics.services.CustomerSegmentationService;
import org.example.graduationproject.analytics.services.RFMAnalysisService;
import org.example.graduationproject.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    @Autowired
    private CustomerSegmentationService segmentationService;
    
    @Autowired
    private RFMAnalysisService rfmAnalysisService;
    
    // ==================== GET ENDPOINTS ====================
    
    @GetMapping("/rfm-analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RFMData>> getRFMAnalysis() {
        List<RFMData> rfmData = rfmAnalysisService.getAllRFMData();
        return ResponseEntity.ok(rfmData);
    }
    
    @GetMapping("/customer-segments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerCluster>> getCustomerSegments() {
        List<CustomerCluster> segments = segmentationService.getCustomerSegments();
        return ResponseEntity.ok(segments);
    }
    
    @GetMapping("/customer/{userId}/segment")
    @PreAuthorize("hasRole('ADMIN') or @authenticationService.getCurrentUser().getId() == #userId")
    public ResponseEntity<CustomerCluster> getCustomerSegment(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        CustomerCluster segment = segmentationService.getCustomerSegment(user);
        return ResponseEntity.ok(segment);
    }
    
    @GetMapping("/segment/{clusterId}/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RFMData>> getCustomersInSegment(@PathVariable Integer clusterId) {
        List<RFMData> customers = segmentationService.getCustomersInSegment(clusterId);
        return ResponseEntity.ok(customers);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    @PostMapping("/rfm/calculate-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> calculateRFMForAllCustomers() {
        try {
            rfmAnalysisService.calculateRFMForAllCustomers();
            return ResponseEntity.ok("RFM analysis completed successfully for all customers");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error calculating RFM: " + e.getMessage());
        }
    }
    
    @PostMapping("/rfm/calculate-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> calculateRFMForUser(@PathVariable Long userId) {
        try {
            User user = new User();
            user.setId(userId);
            rfmAnalysisService.calculateRFMForUser(user);
            return ResponseEntity.ok("RFM analysis completed successfully for user " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error calculating RFM for user: " + e.getMessage());
        }
    }
    
    @PostMapping("/clustering/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> runClustering() {
        try {
            ClusterResult result = segmentationService.performCustomerSegmentation();
            return ResponseEntity.ok("Clustering completed successfully. Total customers: " + result.getTotalCustomers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error running clustering: " + e.getMessage());
        }
    }
    
    @PostMapping("/clusters/refresh-customer-counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> refreshCustomerCounts() {
        try {
            rfmAnalysisService.refreshAllCustomerCounts();
            return ResponseEntity.ok("Customer counts refreshed successfully for all clusters");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
