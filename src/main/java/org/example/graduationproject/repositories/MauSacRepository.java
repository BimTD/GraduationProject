package org.example.graduationproject.repositories;

import org.example.graduationproject.models.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
 
@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    Page<MauSac> findAll(Pageable pageable);
    Page<MauSac> findByMaMauContainingIgnoreCase(String maMau, Pageable pageable);
    
    // Load màu sắc cùng với category
    @Query("SELECT m FROM MauSac m LEFT JOIN FETCH m.loai")
    List<MauSac> findAllWithCategory();
    
    // Thêm phương thức mới để tìm màu sắc theo loại
    List<MauSac> findByLoaiId(Integer loaiId);
} 