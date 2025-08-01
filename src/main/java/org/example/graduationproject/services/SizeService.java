package org.example.graduationproject.services;

import org.example.graduationproject.models.Size;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SizeService {
    List<Size> findAll();
    List<Size> findAllWithCategory();
    Size save(Size size);
    void deleteById(Integer id);
    Optional<Size> findById(Integer id);
    Page<Size> findAll(Pageable pageable);
    Page<Size> findByTenSizeContainingIgnoreCase(String tenSize, Pageable pageable);
} 