package org.example.graduationproject.services;

import org.example.graduationproject.models.Size;
import java.util.List;
import java.util.Optional;

public interface SizeService {
    List<Size> findAll();
    Size save(Size size);
    void deleteById(Integer id);
    Optional<Size> findById(Integer id);
} 