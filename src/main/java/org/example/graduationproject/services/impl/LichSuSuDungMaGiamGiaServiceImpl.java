package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.LichSuSuDungMaGiamGia;
import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.LichSuSuDungMaGiamGiaRepository;
import org.example.graduationproject.services.LichSuSuDungMaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LichSuSuDungMaGiamGiaServiceImpl implements LichSuSuDungMaGiamGiaService {
    
    @Autowired
    private LichSuSuDungMaGiamGiaRepository lichSuSuDungMaGiamGiaRepository;
    
    @Override
    public boolean hasUserUsedMaGiamGia(User user, String maGiamGiaCode) {
        if (user == null || maGiamGiaCode == null || maGiamGiaCode.trim().isEmpty()) {
            return false;
        }
        return lichSuSuDungMaGiamGiaRepository.existsByUserAndMaGiamGiaCode(user, maGiamGiaCode.trim());
    }
    
    @Override
    public LichSuSuDungMaGiamGia saveUsageHistory(User user, MaGiamGia maGiamGia, BigDecimal giaTriGiamGia, Long donHangId) {
        if (user == null || maGiamGia == null) {
            throw new IllegalArgumentException("User và MaGiamGia không được null");
        }
        
        LichSuSuDungMaGiamGia usageHistory = new LichSuSuDungMaGiamGia(user, maGiamGia, giaTriGiamGia, donHangId);
        return lichSuSuDungMaGiamGiaRepository.save(usageHistory);
    }
    
    @Override
    public List<String> getUsedMaGiamGiaCodesByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        return lichSuSuDungMaGiamGiaRepository.findUsedMaGiamGiaCodesByUser(user);
    }
    
    @Override
    public List<LichSuSuDungMaGiamGia> getUsageHistoryByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        return lichSuSuDungMaGiamGiaRepository.findByUserOrderByThoiGianSuDungDesc(user);
    }
    
    @Override
    public List<LichSuSuDungMaGiamGia> saveMultipleUsageHistory(User user, List<MaGiamGia> maGiamGiaList, List<BigDecimal> giaTriGiamGiaList, Long donHangId) {
        if (user == null || maGiamGiaList == null || giaTriGiamGiaList == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }
        
        if (maGiamGiaList.size() != giaTriGiamGiaList.size()) {
            throw new IllegalArgumentException("Số lượng mã giảm giá và giá trị giảm giá không khớp");
        }
        
        List<LichSuSuDungMaGiamGia> usageHistories = new ArrayList<>();
        
        for (int i = 0; i < maGiamGiaList.size(); i++) {
            MaGiamGia maGiamGia = maGiamGiaList.get(i);
            BigDecimal giaTriGiamGia = giaTriGiamGiaList.get(i);
            
            if (maGiamGia != null && giaTriGiamGia != null) {
                LichSuSuDungMaGiamGia usageHistory = new LichSuSuDungMaGiamGia(user, maGiamGia, giaTriGiamGia, donHangId);
                usageHistories.add(lichSuSuDungMaGiamGiaRepository.save(usageHistory));
            }
        }
        
        return usageHistories;
    }
}

