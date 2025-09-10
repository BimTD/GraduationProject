package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.User;
import org.example.graduationproject.repositories.MaGiamGiaRepository;
import org.example.graduationproject.services.MaGiamGiaService;
import org.example.graduationproject.services.LichSuSuDungMaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaGiamGiaServiceImpl implements MaGiamGiaService {

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;
    
    @Autowired
    private LichSuSuDungMaGiamGiaService lichSuSuDungMaGiamGiaService;

    @Override
    @Transactional
    public MaGiamGia createMaGiamGia(MaGiamGia maGiamGia) {
        // Kiểm tra mã giảm giá đã tồn tại chưa
        if (maGiamGiaRepository.findByMaGiamGia(maGiamGia.getMaGiamGia()).isPresent()) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
        }
        
        maGiamGia.setNgayTao(LocalDateTime.now());
        maGiamGia.setNgayCapNhat(LocalDateTime.now());
        maGiamGia.setSoLuongDaSuDung(0);
        
        return maGiamGiaRepository.save(maGiamGia);
    }

    @Override
    @Transactional
    public MaGiamGia updateMaGiamGia(MaGiamGia maGiamGia) {
        MaGiamGia existingMaGiamGia = maGiamGiaRepository.findById(maGiamGia.getId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        
        // Kiểm tra mã code có thay đổi không và có bị trùng không
        if (!existingMaGiamGia.getMaGiamGia().equals(maGiamGia.getMaGiamGia())) {
            // Nếu mã code thay đổi, kiểm tra xem có mã nào khác đã sử dụng mã này chưa
            Optional<MaGiamGia> existingWithSameCode = maGiamGiaRepository.findByMaGiamGia(maGiamGia.getMaGiamGia());
            if (existingWithSameCode.isPresent() && !existingWithSameCode.get().getId().equals(maGiamGia.getId())) {
                throw new RuntimeException("Mã giảm giá đã tồn tại");
            }
        }
        
        // Giữ nguyên các giá trị không được cập nhật
        maGiamGia.setNgayTao(existingMaGiamGia.getNgayTao());
        maGiamGia.setSoLuongDaSuDung(existingMaGiamGia.getSoLuongDaSuDung());
        maGiamGia.setNgayCapNhat(LocalDateTime.now());
        
        return maGiamGiaRepository.save(maGiamGia);
    }

    @Override
    public MaGiamGia getMaGiamGiaById(Integer id) {
        return maGiamGiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
    }

    @Override
    public MaGiamGia getMaGiamGiaByCode(String maGiamGia) {
        return maGiamGiaRepository.findByMaGiamGia(maGiamGia)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
    }

    @Override
    public List<MaGiamGia> getAllMaGiamGia() {
        return maGiamGiaRepository.findAll();
    }

    @Override
    public List<MaGiamGia> getActiveMaGiamGia() {
        return maGiamGiaRepository.findAllActive(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteMaGiamGia(Integer id) {
        MaGiamGia maGiamGia = maGiamGiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        
        // Kiểm tra mã đã được sử dụng chưa
        if (maGiamGia.getSoLuongDaSuDung() > 0) {
            throw new RuntimeException("Không thể xóa mã giảm giá đã được sử dụng");
        }
        
        maGiamGiaRepository.delete(maGiamGia);
    }

    @Override
    public boolean isValidMaGiamGia(String maGiamGia) {
        return maGiamGiaRepository.existsActiveByMaGiamGia(maGiamGia, LocalDateTime.now());
    }

    @Override
    public BigDecimal calculateDiscountAmount(String maGiamGia, BigDecimal orderTotal, List<SanPham> products) {
        Optional<MaGiamGia> optionalMaGiamGia = maGiamGiaRepository.findActiveByMaGiamGia(maGiamGia, LocalDateTime.now());
        
        if (optionalMaGiamGia.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        MaGiamGia discountCode = optionalMaGiamGia.get();
        
        // Kiểm tra giá trị tối thiểu
        if (discountCode.getGiaTriToiThieu() != null && 
            orderTotal.compareTo(discountCode.getGiaTriToiThieu()) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Kiểm tra có thể áp dụng cho sản phẩm không
        if (!discountCode.getApDungChoTatCa()) {
            boolean canApply = false;
            for (SanPham product : products) {
                if (discountCode.canApplyToProduct(product)) {
                    canApply = true;
                    break;
                }
            }
            if (!canApply) {
                return BigDecimal.ZERO;
            }
        }
        
        return discountCode.calculateDiscountAmount(orderTotal);
    }

    @Override
    @Transactional
    public void applyMaGiamGia(String maGiamGia) {
        MaGiamGia discountCode = maGiamGiaRepository.findByMaGiamGia(maGiamGia)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        
        // Kiểm tra còn có thể sử dụng không
        if (discountCode.getSoLuongSuDung() != null && 
            discountCode.getSoLuongDaSuDung() >= discountCode.getSoLuongSuDung()) {
            throw new RuntimeException("Mã giảm giá đã hết số lần sử dụng");
        }
        
        // Tăng số lần đã sử dụng
        discountCode.setSoLuongDaSuDung(discountCode.getSoLuongDaSuDung() + 1);
        discountCode.setNgayCapNhat(LocalDateTime.now());
        
        maGiamGiaRepository.save(discountCode);
    }

    @Override
    public boolean canApplyToProducts(String maGiamGia, List<SanPham> products) {
        Optional<MaGiamGia> optionalMaGiamGia = maGiamGiaRepository.findActiveByMaGiamGia(maGiamGia, LocalDateTime.now());
        
        if (optionalMaGiamGia.isEmpty()) {
            return false;
        }
        
        MaGiamGia discountCode = optionalMaGiamGia.get();
        
        if (discountCode.getApDungChoTatCa()) {
            return true;
        }
        
        for (SanPham product : products) {
            if (discountCode.canApplyToProduct(product)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public List<MaGiamGia> getApplicableMaGiamGiaForProducts(List<SanPham> products) {
        if (products == null || products.isEmpty()) {
            return getActiveMaGiamGia();
        }
        
        // Lấy tất cả mã giảm giá có thể áp dụng cho ít nhất một sản phẩm
        List<MaGiamGia> applicableCodes = getActiveMaGiamGia();
        
        return applicableCodes.stream()
            .filter(code -> {
                if (code.getApDungChoTatCa()) {
                    return true;
                }
                return products.stream().anyMatch(code::canApplyToProduct);
            })
            .toList();
    }

    @Override
    @Transactional
    public void updateMaGiamGiaStatus(Integer id, String status) {
        MaGiamGia maGiamGia = maGiamGiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        
        maGiamGia.setTrangThai(status);
        maGiamGia.setNgayCapNhat(LocalDateTime.now());
        
        maGiamGiaRepository.save(maGiamGia);
    }

    @Override
    @Transactional
    public void updateExpiredMaGiamGia() {
        List<MaGiamGia> expiredCodes = maGiamGiaRepository.findExpiredCodes(LocalDateTime.now());
        
        for (MaGiamGia code : expiredCodes) {
            code.setTrangThai("EXPIRED");
            code.setNgayCapNhat(LocalDateTime.now());
        }
        
        if (!expiredCodes.isEmpty()) {
            maGiamGiaRepository.saveAll(expiredCodes);
        }
        
        // Cập nhật mã hết số lần sử dụng
        List<MaGiamGia> exhaustedCodes = maGiamGiaRepository.findExhaustedCodes();
        
        for (MaGiamGia code : exhaustedCodes) {
            code.setTrangThai("EXHAUSTED");
            code.setNgayCapNhat(LocalDateTime.now());
        }
        
        if (!exhaustedCodes.isEmpty()) {
            maGiamGiaRepository.saveAll(exhaustedCodes);
        }
    }
    
    @Override
    public List<MaGiamGia> getAvailableMaGiamGiaForUser(User user) {
        if (user == null) {
            return List.of();
        }
        
        // Lấy tất cả mã giảm giá active
        List<MaGiamGia> activeCodes = getActiveMaGiamGia();
        
        // Lấy danh sách mã đã sử dụng bởi user
        List<String> usedCodes = lichSuSuDungMaGiamGiaService.getUsedMaGiamGiaCodesByUser(user);
        
        // Lọc ra những mã chưa được user sử dụng
        return activeCodes.stream()
            .filter(code -> !usedCodes.contains(code.getMaGiamGia()))
            .toList();
    }
    
    @Override
    public boolean canUserUseMaGiamGia(String maGiamGia, User user) {
        if (user == null || maGiamGia == null || maGiamGia.trim().isEmpty()) {
            return false;
        }
        
        // Kiểm tra mã giảm giá có hợp lệ không
        if (!isValidMaGiamGia(maGiamGia)) {
            return false;
        }
        
        // Kiểm tra user đã sử dụng mã này chưa
        return !lichSuSuDungMaGiamGiaService.hasUserUsedMaGiamGia(user, maGiamGia);
    }
}
