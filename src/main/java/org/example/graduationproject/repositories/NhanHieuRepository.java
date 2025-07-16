package org.example.graduationproject.repositories;

import org.example.graduationproject.models.NhanHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NhanHieuRepository extends JpaRepository<NhanHieu, Integer> {
    Page<NhanHieu> findByTenContainingIgnoreCase(String ten, Pageable pageable);
} 