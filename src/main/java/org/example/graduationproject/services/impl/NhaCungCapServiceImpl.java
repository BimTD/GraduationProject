package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.repositories.NhaCungCapRepository;
import org.example.graduationproject.services.NhaCungCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NhaCungCapServiceImpl implements NhaCungCapService {
    @Autowired
    private NhaCungCapRepository nhaCungCapRepository;

    @Override
    public List<NhaCungCap> getAllNhaCungCap() {
        return nhaCungCapRepository.findAll();
    }

    @Override
    public Page<NhaCungCap> getAllNhaCungCapPaging(int page, int size) {
        return nhaCungCapRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<NhaCungCap> searchNhaCungCapByTenPaging(String ten, int page, int size) {
        return nhaCungCapRepository.findByTenContainingIgnoreCase(ten, PageRequest.of(page, size));
    }

    @Override
    public NhaCungCap save(NhaCungCap nhaCungCap) {
        return nhaCungCapRepository.save(nhaCungCap);
    }

    @Override
    public Optional<NhaCungCap> findById(Integer id) {
        return nhaCungCapRepository.findById(id);
    }

    @Override
    public void deleteById(Integer id) {
        nhaCungCapRepository.deleteById(id);
    }
}
