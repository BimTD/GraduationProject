package org.example.graduationproject.analytics.algorithms;

import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.models.RFMData;
import org.example.graduationproject.analytics.models.RFMStats;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class KMeansAlgorithm {

    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 0.001;

    public List<CustomerCluster> performKMeans(List<RFMData> rfmDataList, int k) {
        if (rfmDataList.isEmpty() || k <= 0) {
            return new ArrayList<>();
        }

        // Chuẩn hóa dữ liệu
        RFMStats stats = RFMStats.calculateStats(rfmDataList);
        List<RFMData> normalizedData = normalizeData(rfmDataList, stats);

        // Khởi tạo centroids ngẫu nhiên
        List<CustomerCluster> centroids = initializeCentroids(k, normalizedData);

        boolean converged = false;
        int iteration = 0;

        while (!converged && iteration < MAX_ITERATIONS) {
            // Gán mỗi điểm dữ liệu vào cụm gần nhất
            assignToClusters(normalizedData, centroids);

            // Cập nhật centroids
            List<CustomerCluster> newCentroids = updateCentroids(normalizedData, k);

            // Kiểm tra hội tụ
            converged = checkConvergence(centroids, newCentroids);
            centroids = newCentroids;
            iteration++;
        }

        // Chuyển đổi centroids về thang đo gốc
        return denormalizeCentroids(centroids, stats);
    }

    private List<RFMData> normalizeData(List<RFMData> rfmDataList, RFMStats stats) {
        return rfmDataList.stream().map(rfm -> {
            RFMData normalized = new RFMData(rfm.getUser(), rfm.getRecencyScore(),
                    rfm.getFrequencyScore(), rfm.getMonetaryScore());

            // Min-Max normalization
            normalized.setNormalizedRecency(
                    (stats.getMaxRecency() - rfm.getRecencyScore()) /
                            (double)(stats.getMaxRecency() - stats.getMinRecency())
            );

            normalized.setNormalizedFrequency(
                    (rfm.getFrequencyScore() - stats.getMinFrequency()) /
                            (double)(stats.getMaxFrequency() - stats.getMinFrequency())
            );

            normalized.setNormalizedMonetary(
                    (rfm.getMonetaryScore().doubleValue() - stats.getMinMonetary().doubleValue()) /
                            (stats.getMaxMonetary().doubleValue() - stats.getMinMonetary().doubleValue())
            );

            return normalized;
        }).collect(Collectors.toList());
    }

    private List<CustomerCluster> initializeCentroids(int k, List<RFMData> normalizedData) {
        List<CustomerCluster> centroids = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < k; i++) {
            CustomerCluster centroid = new CustomerCluster();
            centroid.setId(i + 1);
            centroid.setClusterName("Cluster " + (i + 1));

            // Khởi tạo ngẫu nhiên trong khoảng [0,1]
            centroid.setNormalizedRecency(random.nextDouble());
            centroid.setNormalizedFrequency(random.nextDouble());
            centroid.setNormalizedMonetary(random.nextDouble());

            centroids.add(centroid);
        }

        return centroids;
    }

    private void assignToClusters(List<RFMData> normalizedData, List<CustomerCluster> centroids) {
        for (RFMData rfm : normalizedData) {
            double minDistance = Double.MAX_VALUE;
            int closestCluster = 0;

            for (int i = 0; i < centroids.size(); i++) {
                double distance = calculateDistance(rfm, centroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCluster = i + 1;
                }
            }

            rfm.setClusterId(closestCluster);
        }
    }

    private double calculateDistance(RFMData point, CustomerCluster centroid) {
        double rDiff = point.getNormalizedRecency() - centroid.getNormalizedRecency();
        double fDiff = point.getNormalizedFrequency() - centroid.getNormalizedFrequency();
        double mDiff = point.getNormalizedMonetary() - centroid.getNormalizedMonetary();

        return Math.sqrt(rDiff * rDiff + fDiff * fDiff + mDiff * mDiff);
    }

    private List<CustomerCluster> updateCentroids(List<RFMData> normalizedData, int k) {
        List<CustomerCluster> newCentroids = new ArrayList<>();

        for (int i = 1; i <= k; i++) {
            final int clusterId = i;
            List<RFMData> clusterPoints = normalizedData.stream()
                    .filter(rfm -> rfm.getClusterId() == clusterId)
                    .collect(Collectors.toList());

            if (clusterPoints.isEmpty()) {
                // Nếu cụm trống, giữ nguyên centroid cũ
                newCentroids.add(new CustomerCluster());
                continue;
            }

            CustomerCluster newCentroid = new CustomerCluster();
            newCentroid.setId(clusterId);
            newCentroid.setClusterName("Cluster " + clusterId);

            // Tính centroid mới
            double avgR = clusterPoints.stream()
                    .mapToDouble(RFMData::getNormalizedRecency)
                    .average().orElse(0.0);

            double avgF = clusterPoints.stream()
                    .mapToDouble(RFMData::getNormalizedFrequency)
                    .average().orElse(0.0);

            double avgM = clusterPoints.stream()
                    .mapToDouble(RFMData::getNormalizedMonetary)
                    .average().orElse(0.0);

            newCentroid.setNormalizedRecency(avgR);
            newCentroid.setNormalizedFrequency(avgF);
            newCentroid.setNormalizedMonetary(avgM);
            newCentroid.setCustomerCount(clusterPoints.size());

            newCentroids.add(newCentroid);
        }

        return newCentroids;
    }

    private boolean checkConvergence(List<CustomerCluster> oldCentroids, List<CustomerCluster> newCentroids) {
        for (int i = 0; i < oldCentroids.size(); i++) {
            double rDiff = Math.abs(oldCentroids.get(i).getNormalizedRecency() -
                    newCentroids.get(i).getNormalizedRecency());
            double fDiff = Math.abs(oldCentroids.get(i).getNormalizedFrequency() -
                    newCentroids.get(i).getNormalizedFrequency());
            double mDiff = Math.abs(oldCentroids.get(i).getNormalizedMonetary() -
                    newCentroids.get(i).getNormalizedMonetary());

            if (rDiff > CONVERGENCE_THRESHOLD || fDiff > CONVERGENCE_THRESHOLD ||
                    mDiff > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    private List<CustomerCluster> denormalizeCentroids(List<CustomerCluster> centroids, RFMStats stats) {
        return centroids.stream().map(centroid -> {
            CustomerCluster denormalized = new CustomerCluster();
            denormalized.setId(centroid.getId());
            denormalized.setClusterName(centroid.getClusterName());
            denormalized.setCustomerCount(centroid.getCustomerCount());
            denormalized.setCreatedDate(LocalDateTime.now());

            // Chuyển đổi về thang đo gốc
            denormalized.setRecencyAvg(BigDecimal.valueOf(
                    stats.getMaxRecency() - (centroid.getNormalizedRecency() *
                            (stats.getMaxRecency() - stats.getMinRecency()))
            ));

            denormalized.setFrequencyAvg(BigDecimal.valueOf(
                    stats.getMinFrequency() + (centroid.getNormalizedFrequency() *
                            (stats.getMaxFrequency() - stats.getMinFrequency()))
            ));

            denormalized.setMonetaryAvg(BigDecimal.valueOf(
                    stats.getMinMonetary().doubleValue() + (centroid.getNormalizedMonetary() *
                            (stats.getMaxMonetary().doubleValue() - stats.getMinMonetary().doubleValue()))
            ));

            return denormalized;
        }).collect(Collectors.toList());
    }
}