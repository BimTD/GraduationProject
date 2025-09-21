package org.example.graduationproject.repositories;

import org.example.graduationproject.models.ChatMessage;
import org.example.graduationproject.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user = :user ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user = :user AND cm.isRead = false ORDER BY cm.createdAt ASC")
    List<ChatMessage> findUnreadMessagesByUser(@Param("user") User user);
    
    long countByUserAndIsReadFalse(User user);
}






















