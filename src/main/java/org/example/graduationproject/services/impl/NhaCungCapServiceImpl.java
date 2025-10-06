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
        // Kiểm tra tên nhà cung cấp có trùng không
        if (!isTenAvailable(nhaCungCap.getTen(), nhaCungCap.getId())) {
            throw new RuntimeException("Tên nhà cung cấp '" + nhaCungCap.getTen() + "' đã tồn tại. Vui lòng chọn tên khác.");
        }
        
        // Kiểm tra số điện thoại có trùng không
        if (nhaCungCap.getSdt() != null && !nhaCungCap.getSdt().trim().isEmpty()) {
            if (!isSdtAvailable(nhaCungCap.getSdt(), nhaCungCap.getId())) {
                throw new RuntimeException("Số điện thoại '" + nhaCungCap.getSdt() + "' đã tồn tại. Vui lòng chọn số điện thoại khác.");
            }
        }
        
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
    
    @Override
    public boolean isTenAvailable(String ten, Integer excludeId) {
        if (ten == null || ten.trim().isEmpty()) {
            return false;
        }
        
        // Nếu có excludeId (chế độ edit), kiểm tra tên trùng với các nhà cung cấp khác
        if (excludeId != null) {
            return !nhaCungCapRepository.existsByTenIgnoreCaseAndIdNot(ten.trim(), excludeId);
        } else {
            // Nếu không có excludeId (chế độ thêm mới), kiểm tra tên trùng với tất cả nhà cung cấp
            return !nhaCungCapRepository.existsByTenIgnoreCase(ten.trim());
        }
    }
    
    @Override
    public boolean isSdtAvailable(String sdt, Integer excludeId) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return false;
        }
        
        // Nếu có excludeId (chế độ edit), kiểm tra số điện thoại trùng với các nhà cung cấp khác
        if (excludeId != null) {
            return !nhaCungCapRepository.existsBySdtAndIdNot(sdt.trim(), excludeId);
        } else {
            // Nếu không có excludeId (chế độ thêm mới), kiểm tra số điện thoại trùng với tất cả nhà cung cấp
            return !nhaCungCapRepository.existsBySdt(sdt.trim());
        }
    }
}
