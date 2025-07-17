package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.Loai;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.services.LoaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoaiServiceImpl implements LoaiService {
    @Autowired
    private LoaiRepository loaiRepository;

    public List<Loai> getAllLoai() {
        return loaiRepository.findAll();
    }

    public Optional<Loai> getLoaiById(Integer id) {
        return loaiRepository.findById(id);
    }

    public Loai saveLoai(Loai loai) {
        return loaiRepository.save(loai);
    }

    public void deleteLoai(Integer id) {
        loaiRepository.deleteById(id);
    }

    public List<Loai> searchLoaiByTen(String ten) {
        return loaiRepository.findByTenContainingIgnoreCase(ten);
    }

    public Page<Loai> getAllLoaiPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return loaiRepository.findAll(pageable);
    }

    public Page<Loai> searchLoaiByTenPaging(String ten, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return loaiRepository.findByTenContainingIgnoreCase(ten, pageable);
    }
}
