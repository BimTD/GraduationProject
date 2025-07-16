package org.example.graduationproject.repositories;

import org.example.graduationproject.models.Loai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoaiRepository extends JpaRepository<Loai, Integer> {
    // Tìm kiếm theo tên (không phân biệt hoa thường)
    List<Loai> findByTenContainingIgnoreCase(String ten);
    Page<Loai> findByTenContainingIgnoreCase(String ten, Pageable pageable);
} 