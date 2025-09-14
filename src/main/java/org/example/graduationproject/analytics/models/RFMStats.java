package org.example.graduationproject.analytics.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RFMStats {
    // Recency stats
    private int minRecency, maxRecency, meanRecency, stdRecency;
    private int medianRecency, q1Recency, q3Recency;
    
    // Frequency stats
    private int minFrequency, maxFrequency, meanFrequency, stdFrequency;
    private int medianFrequency, q1Frequency, q3Frequency;
    
    // Monetary stats
    private BigDecimal minMonetary, maxMonetary, meanMonetary, stdMonetary;
    private BigDecimal medianMonetary, q1Monetary, q3Monetary;
    
    public static RFMStats calculateStats(List<RFMData> rfmDataList) {
        if (rfmDataList.isEmpty()) {
            return new RFMStats();
        }
        
        RFMStats stats = new RFMStats();
        
        // Calculate Recency stats
        List<Integer> recencyValues = rfmDataList.stream()
            .map(RFMData::getRecencyScore)
            .sorted()
            .collect(Collectors.toList());
        
        stats.minRecency = recencyValues.get(0);
        stats.maxRecency = recencyValues.get(recencyValues.size() - 1);
        stats.meanRecency = (int) recencyValues.stream().mapToInt(Integer::intValue).average().orElse(0);
        stats.medianRecency = recencyValues.get(recencyValues.size() / 2);
        
        // Calculate Frequency stats
        List<Integer> frequencyValues = rfmDataList.stream()
            .map(RFMData::getFrequencyScore)
            .sorted()
            .collect(Collectors.toList());
        
        stats.minFrequency = frequencyValues.get(0);
        stats.maxFrequency = frequencyValues.get(frequencyValues.size() - 1);
        stats.meanFrequency = (int) frequencyValues.stream().mapToInt(Integer::intValue).average().orElse(0);
        stats.medianFrequency = frequencyValues.get(frequencyValues.size() / 2);
        
        // Calculate Monetary stats
        List<BigDecimal> monetaryValues = rfmDataList.stream()
            .map(RFMData::getMonetaryScore)
            .sorted()
            .collect(Collectors.toList());
        
        stats.minMonetary = monetaryValues.get(0);
        stats.maxMonetary = monetaryValues.get(monetaryValues.size() - 1);
        stats.meanMonetary = monetaryValues.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(monetaryValues.size()), 2, BigDecimal.ROUND_HALF_UP);
        stats.medianMonetary = monetaryValues.get(monetaryValues.size() / 2);
        
        return stats;
    }
}
