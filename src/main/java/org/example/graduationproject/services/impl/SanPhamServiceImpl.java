package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Override
    public List<SanPham> getAll() {
        return this.sanPhamRepository.findAll();
    }

    @Override
    public Boolean create(SanPham sanPham) {
        try{
            this.sanPhamRepository.save(sanPham);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public SanPham findById(Integer id) {
        return this.sanPhamRepository.findById(id).orElse(null);
    }

    @Override
    public Boolean deleteById(Integer id) {
        try {
            this.sanPhamRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean update(SanPham sanPham) {
        try {
            this.sanPhamRepository.save(sanPham);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SanPham save(SanPham sanPham) {
        return this.sanPhamRepository.save(sanPham);
    }
}
