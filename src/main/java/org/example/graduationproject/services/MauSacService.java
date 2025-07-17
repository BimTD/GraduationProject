package org.example.graduationproject.services;

import org.example.graduationproject.models.MauSac;
import java.util.List;
import java.util.Optional;

public interface MauSacService {
    List<MauSac> findAll();
    MauSac save(MauSac color);
    void deleteById(Integer id);
    Optional<MauSac> findById(Integer id);
} 