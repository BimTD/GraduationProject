package org.example.graduationproject.repositories;

import org.example.graduationproject.models.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
 
@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    Page<MauSac> findAll(Pageable pageable);
    Page<MauSac> findByMaMauContainingIgnoreCase(String maMau, Pageable pageable);
} 