package org.example.graduationproject.analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "analytics")
public class AnalyticsConfig {
    
    private Clustering clustering = new Clustering();
    private Rfm rfm = new Rfm();
    
    public static class Clustering {
        private boolean enabled = true;
        private int k = 4;
        private String cron = "0 0 3 * * MON";
        
        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getK() { return k; }
        public void setK(int k) { this.k = k; }
        
        public String getCron() { return cron; }
        public void setCron(String cron) { this.cron = cron; }
    }
    
    public static class Rfm {
        private String updateCron = "0 0 2 * * ?";
        
        // Getters and setters
        public String getUpdateCron() { return updateCron; }
        public void setUpdateCron(String updateCron) { this.updateCron = updateCron; }
    }
    
    // Getters and setters
    public Clustering getClustering() { return clustering; }
    public void setClustering(Clustering clustering) { this.clustering = clustering; }
    
    public Rfm getRfm() { return rfm; }
    public void setRfm(Rfm rfm) { this.rfm = rfm; }
}























