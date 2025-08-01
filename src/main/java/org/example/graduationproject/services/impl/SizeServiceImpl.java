package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.Size;
import org.example.graduationproject.repositories.SizeRepository;
import org.example.graduationproject.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SizeServiceImpl implements SizeService {
    @Autowired
    private SizeRepository sizeRepository;

    @Override
    public List<Size> findAll() { return sizeRepository.findAll(); }

    @Override
    public List<Size> findAllWithCategory() { return sizeRepository.findAllWithCategory(); }

    @Override
    public Page<Size> findAll(Pageable pageable) { return sizeRepository.findAll(pageable); }

    @Override
    public Size save(Size size) { return sizeRepository.save(size); }

    @Override
    public void deleteById(Integer id) { sizeRepository.deleteById(id); }

    @Override
    public Optional<Size> findById(Integer id) { return sizeRepository.findById(id); }

    @Override
    public Page<Size> findByTenSizeContainingIgnoreCase(String tenSize, Pageable pageable) { return sizeRepository.findByTenSizeContainingIgnoreCase(tenSize, pageable); }
} 
 