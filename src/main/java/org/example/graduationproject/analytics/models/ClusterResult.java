package org.example.graduationproject.analytics.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClusterResult {
    private List<CustomerCluster> clusters;
    private List<RFMData> rfmData;
    private int totalCustomers;
    private String analysisDate;
    private double silhouetteScore; // Đánh giá chất lượng clustering
}


