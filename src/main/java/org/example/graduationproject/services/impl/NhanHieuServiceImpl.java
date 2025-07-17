package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.NhanHieu;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.services.NhanHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NhanHieuServiceImpl implements NhanHieuService {
    @Autowired
    private NhanHieuRepository nhanHieuRepository;

    public Page<NhanHieu> getAllNhanHieuPaging(int page, int size) {
        return nhanHieuRepository.findAll(PageRequest.of(page, size));
    }

    public Page<NhanHieu> searchNhanHieuByTenPaging(String ten, int page, int size) {
        return nhanHieuRepository.findByTenContainingIgnoreCase(ten, PageRequest.of(page, size));
    }

    public NhanHieu save(NhanHieu nhanHieu) {
        return nhanHieuRepository.save(nhanHieu);
    }

    public Optional<NhanHieu> findById(Integer id) {
        return nhanHieuRepository.findById(id);
    }

    public void deleteById(Integer id) {
        nhanHieuRepository.deleteById(id);
    }
}
