package org.example.graduationproject.repositories;

import org.example.graduationproject.models.SePayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SePayTransactionRepository extends JpaRepository<SePayTransaction, Long> {
    
    Optional<SePayTransaction> findByTransactionId(String transactionId);
    
    Optional<SePayTransaction> findByOrderId(String orderId);
    
    List<SePayTransaction> findByUserIdAndStatus(Long userId, String status);
    
    List<SePayTransaction> findByStatus(String status);
    
    @Query("SELECT s FROM SePayTransaction s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<SePayTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT s FROM SePayTransaction s WHERE s.status = 'PENDING' AND s.createdAt < :expiredTime")
    List<SePayTransaction> findExpiredPendingTransactions(@Param("expiredTime") java.time.LocalDateTime expiredTime);
}
