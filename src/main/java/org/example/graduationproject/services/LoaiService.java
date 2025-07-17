package org.example.graduationproject.services;

import org.example.graduationproject.models.Loai;
import org.example.graduationproject.repositories.LoaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface LoaiService {
    List<Loai> getAllLoai();
    Optional<Loai> getLoaiById(Integer id);
    Loai saveLoai(Loai loai);
    void deleteLoai(Integer id);
    List<Loai> searchLoaiByTen(String ten);
    Page<Loai> getAllLoaiPaging(int page, int size);
    Page<Loai> searchLoaiByTenPaging(String ten, int page, int size);
}

