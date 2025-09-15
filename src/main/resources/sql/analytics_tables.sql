-- SQL Scripts for RFM Analysis and Customer Clustering
-- Run these scripts in your SQL Server database

-- 1. Create RFMAnalysis table
CREATE TABLE RFMAnalysis (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recency_score INT NOT NULL,
    frequency_score INT NOT NULL,
    monetary_score DECIMAL(18,2) NOT NULL,
    rfm_score NVARCHAR(10),
    cluster_id INT,
    analysis_date DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX IX_RFMAnalysis_User ON RFMAnalysis(user_id);
CREATE INDEX IX_RFMAnalysis_Cluster ON RFMAnalysis(cluster_id);
CREATE INDEX IX_RFMAnalysis_Date ON RFMAnalysis(analysis_date);
CREATE INDEX IX_RFMAnalysis_RFM ON RFMAnalysis(recency_score, frequency_score, monetary_score);

-- 2. Create CustomerClusters table
CREATE TABLE CustomerClusters (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cluster_name NVARCHAR(100) NOT NULL,
    cluster_description NVARCHAR(500),
    recency_avg DECIMAL(10,2),
    frequency_avg DECIMAL(10,2),
    monetary_avg DECIMAL(18,2),
    customer_count INT,
    created_date DATETIME2 DEFAULT GETDATE()
);

-- Create index for better performance
CREATE INDEX IX_CustomerClusters_Date ON CustomerClusters(created_date);
CREATE INDEX IX_CustomerClusters_Name ON CustomerClusters(cluster_name);

-- 3. Sample data insertion (optional - for testing)
-- Insert sample clusters
INSERT INTO CustomerClusters (cluster_name, cluster_description, recency_avg, frequency_avg, monetary_avg, customer_count, created_date)
VALUES 
('Champions', 'Khách hàng VIP - mua hàng thường xuyên, giá trị cao', 15.5, 25.2, 8000000.00, 0, GETDATE()),
('Loyal Customers', 'Khách hàng trung thành - mua hàng đều đặn', 45.8, 12.5, 3500000.00, 0, GETDATE()),
('At Risk', 'Khách hàng có nguy cơ rời bỏ - cần chăm sóc đặc biệt', 120.3, 8.2, 1800000.00, 0, GETDATE()),
('Lost', 'Khách hàng đã mất - cần chiến lược win-back', 250.7, 3.1, 800000.00, 0, GETDATE());

-- 4. Create view for easy querying
CREATE VIEW v_CustomerSegments AS
SELECT 
    c.id as cluster_id,
    c.cluster_name,
    c.cluster_description,
    c.recency_avg,
    c.frequency_avg,
    c.monetary_avg,
    c.customer_count,
    c.created_date,
    COUNT(r.id) as actual_customers
FROM CustomerClusters c
LEFT JOIN RFMAnalysis r ON c.id = r.cluster_id
GROUP BY c.id, c.cluster_name, c.cluster_description, c.recency_avg, c.frequency_avg, c.monetary_avg, c.customer_count, c.created_date;

PRINT 'Analytics tables created successfully!';