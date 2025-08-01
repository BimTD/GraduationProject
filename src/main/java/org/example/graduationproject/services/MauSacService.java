package org.example.graduationproject.services;

import org.example.graduationproject.models.MauSac;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MauSacService {
    List<MauSac> findAll();
    List<MauSac> findAllWithCategory();
    MauSac save(MauSac color);
    void deleteById(Integer id);
    Optional<MauSac> findById(Integer id);
    Page<MauSac> findAll(Pageable pageable);
    Page<MauSac> findByMaMauContainingIgnoreCase(String maMau, Pageable pageable);
} 