package org.example.graduationproject.repositories;

import org.example.graduationproject.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    Page<Size> findAll(Pageable pageable);
    Page<Size> findByTenSizeContainingIgnoreCase(String tenSize, Pageable pageable);
    
    // Load size cùng với category
    @Query("SELECT s FROM Size s LEFT JOIN FETCH s.loai")
    List<Size> findAllWithCategory();
    
    // Thêm phương thức mới để tìm size theo loại
    List<Size> findByLoaiId(Integer loaiId);
} 