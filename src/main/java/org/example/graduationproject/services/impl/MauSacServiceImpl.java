package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.MauSac;
import org.example.graduationproject.repositories.MauSacRepository;
import org.example.graduationproject.services.MauSacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MauSacServiceImpl implements MauSacService {
    @Autowired
    private MauSacRepository mauSacRepository;

    @Override
    public List<MauSac> findAll() { return mauSacRepository.findAll(); }

    @Override
    public List<MauSac> findAllWithCategory() { return mauSacRepository.findAllWithCategory(); }

    @Override
    public MauSac save(MauSac color) { return mauSacRepository.save(color); }

    @Override
    public void deleteById(Integer id) { mauSacRepository.deleteById(id); }

    @Override
    public Optional<MauSac> findById(Integer id) { return mauSacRepository.findById(id); }

    @Override
    public Page<MauSac> findAll(Pageable pageable) { return mauSacRepository.findAll(pageable); }

    @Override
    public Page<MauSac> findByMaMauContainingIgnoreCase(String maMau, Pageable pageable) { return mauSacRepository.findByMaMauContainingIgnoreCase(maMau, pageable); }
} 
 