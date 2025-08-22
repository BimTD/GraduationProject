package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.Loai;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.services.LoaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
        // Thay thế derived query bằng QBE
        if (ten == null || ten.trim().isEmpty()) {
            return getAllLoai();
        }
        
        Loai probe = new Loai();
        probe.setTen(ten);
        
        // Sử dụng QBE với ExampleMatcher để có behavior giống như findByTenContainingIgnoreCase
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
            .withIgnoreCase(true)
            .withIgnoreNullValues();
        
        Example<Loai> example = Example.of(probe, matcher);
        return loaiRepository.findAll(example);
    }

    public Page<Loai> getAllLoaiPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return loaiRepository.findAll(pageable);
    }

    public Page<Loai> searchLoaiByTenPaging(String ten, int page, int size) {
        // Thay thế derived query bằng QBE với phân trang
        if (ten == null || ten.trim().isEmpty()) {
            return getAllLoaiPaging(page, size);
        }
        
        Loai probe = new Loai();
        probe.setTen(ten);
        
        // Sử dụng QBE với ExampleMatcher để có behavior giống như findByTenContainingIgnoreCase
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
            .withIgnoreCase(true)
            .withIgnoreNullValues();
        
        Example<Loai> example = Example.of(probe, matcher);
        Pageable pageable = PageRequest.of(page, size);
        return loaiRepository.findAll(example, pageable);
    }
    

}
