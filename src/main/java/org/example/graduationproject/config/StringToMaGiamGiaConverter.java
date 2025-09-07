package org.example.graduationproject.config;

import org.example.graduationproject.models.MaGiamGia;
import org.example.graduationproject.services.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMaGiamGiaConverter implements Converter<String, MaGiamGia> {

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Override
    public MaGiamGia convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Thử convert thành Integer trước (nếu là ID)
            Integer id = Integer.parseInt(source);
            return maGiamGiaService.getMaGiamGiaById(id);
        } catch (NumberFormatException e) {
            // Nếu không phải số, coi như là mã giảm giá
            try {
                return maGiamGiaService.getMaGiamGiaByCode(source);
            } catch (Exception ex) {
                // Nếu không tìm thấy, trả về null
                return null;
            }
        }
    }
}
