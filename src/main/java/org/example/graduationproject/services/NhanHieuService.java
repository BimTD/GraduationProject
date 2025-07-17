package org.example.graduationproject.services;

import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;


public interface NhanHieuService {
    Page<NhanHieu> getAllNhanHieuPaging(int page, int size);
    Page<NhanHieu> searchNhanHieuByTenPaging(String ten, int page, int size);
    NhanHieu save(NhanHieu nhanHieu);
    Optional<NhanHieu> findById(Integer id);
    void deleteById(Integer id);
} 