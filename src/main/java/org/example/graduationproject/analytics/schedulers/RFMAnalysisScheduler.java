package org.example.graduationproject.analytics.schedulers;

import org.example.graduationproject.analytics.services.CustomerSegmentationService;
import org.example.graduationproject.analytics.services.RFMAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RFMAnalysisScheduler {
    
    @Autowired
    private RFMAnalysisService rfmAnalysisService;
    
    @Autowired
    private CustomerSegmentationService segmentationService;
    
    // Chạy lúc 2h sáng hàng ngày để cập nhật RFM
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateRFMAnalysis() {
        try {
            System.out.println("Bắt đầu cập nhật phân tích RFM...");
            rfmAnalysisService.calculateRFMForAllCustomers();
            System.out.println("Hoàn thành cập nhật phân tích RFM.");
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật RFM: " + e.getMessage());
        }
    }
    
    // Chạy lúc 3h sáng thứ 2 hàng tuần để thực hiện clustering
    @Scheduled(cron = "0 0 3 * * MON")
    public void performWeeklyClustering() {
        try {
            System.out.println("Bắt đầu thực hiện phân cụm khách hàng...");
            segmentationService.performCustomerSegmentation();
            System.out.println("Hoàn thành phân cụm khách hàng.");
        } catch (Exception e) {
            System.err.println("Lỗi khi thực hiện clustering: " + e.getMessage());
        }
    }
}

























