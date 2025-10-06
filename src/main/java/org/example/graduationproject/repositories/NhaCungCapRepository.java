package org.example.graduationproject.repositories;

import org.example.graduationproject.models.NhaCungCap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Integer> {
    Page<NhaCungCap> findByTenContainingIgnoreCase(String ten, Pageable pageable);
    
    // Methods for checking uniqueness
    boolean existsByTenIgnoreCase(String ten);
    boolean existsByTenIgnoreCaseAndIdNot(String ten, Integer id);
    boolean existsBySdt(String sdt);
    boolean existsBySdtAndIdNot(String sdt, Integer id);
} 