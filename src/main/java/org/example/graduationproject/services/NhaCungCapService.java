package org.example.graduationproject.services;

import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.repositories.NhaCungCapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface NhaCungCapService {
    List<NhaCungCap> getAllNhaCungCap();
    Page<NhaCungCap> getAllNhaCungCapPaging(int page, int size);
    Page<NhaCungCap> searchNhaCungCapByTenPaging(String ten, int page, int size);
    NhaCungCap save(NhaCungCap nhaCungCap);
    Optional<NhaCungCap> findById(Integer id);
    void deleteById(Integer id);
} 