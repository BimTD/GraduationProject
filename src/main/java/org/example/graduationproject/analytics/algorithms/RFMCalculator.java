package org.example.graduationproject.analytics.algorithms;

import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.models.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RFMCalculator {
    
    public RFMData calculateRFM(User user, List<HoaDon> orders) {
        if (orders.isEmpty()) {
            return new RFMData(user, 999, 0, BigDecimal.ZERO);
        }
        
        // Tính Recency: Số ngày từ lần mua gần nhất
        LocalDateTime lastOrderDate = orders.stream()
            .map(HoaDon::getNgayTao)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
        
        int recency = (int) ChronoUnit.DAYS.between(lastOrderDate, LocalDateTime.now());
        
        // Tính Frequency: Tổng số đơn hàng
        int frequency = orders.size();
        
        // Tính Monetary: Tổng tiền đã chi (sử dụng tongTienSauGiamGia nếu có)
        BigDecimal monetary = orders.stream()
            .map(order -> order.getTongTienSauGiamGia() != null ? 
                order.getTongTienSauGiamGia() : order.getTongTien())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        RFMData rfmData = new RFMData(user, recency, frequency, monetary);
        rfmData.setRfmScore(generateRFMScore(recency, frequency, monetary));
        
        return rfmData;
    }
    
    private String generateRFMScore(int recency, int frequency, BigDecimal monetary) {
        // Chia thành 5 mức độ (1-5)
        int rScore = calculateRecencyScore(recency);
        int fScore = calculateFrequencyScore(frequency);
        int mScore = calculateMonetaryScore(monetary);
        
        return rScore + "-" + fScore + "-" + mScore;
    }
    
    private int calculateRecencyScore(int recency) {
        if (recency <= 30) return 5;
        if (recency <= 60) return 4;
        if (recency <= 90) return 3;
        if (recency <= 180) return 2;
        return 1;
    }
    
    private int calculateFrequencyScore(int frequency) {
        if (frequency >= 20) return 5;
        if (frequency >= 10) return 4;
        if (frequency >= 5) return 3;
        if (frequency >= 2) return 2;
        return 1;
    }
    
    private int calculateMonetaryScore(BigDecimal monetary) {
        if (monetary.compareTo(new BigDecimal("5000000")) >= 0) return 5;
        if (monetary.compareTo(new BigDecimal("2000000")) >= 0) return 4;
        if (monetary.compareTo(new BigDecimal("1000000")) >= 0) return 3;
        if (monetary.compareTo(new BigDecimal("500000")) >= 0) return 2;
        return 1;
    }
}
