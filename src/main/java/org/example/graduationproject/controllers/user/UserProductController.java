package org.example.graduationproject.controllers.user;

import org.example.graduationproject.models.ImageSanPham;
import org.example.graduationproject.models.MauSac;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.SanPhamBienThe;
import org.example.graduationproject.models.Size;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserProductController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @GetMapping("/products/{id}/quick-view")
    public ResponseEntity<?> getQuickView(@PathVariable("id") Integer productId) {
        Optional<SanPham> productOpt = sanPhamRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SanPham product = productOpt.get();
        List<SanPhamBienThe> variants = sanPhamBienTheRepository.findBySanPhamId(productId);

        String image = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ImageSanPham first = product.getImages().get(0);
            image = first != null ? first.getImageName() : null;
        }

        // Distinct sizes
        List<Map<String, Object>> sizes = variants.stream()
                .map(SanPhamBienThe::getSize)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Size::getId,
                                s -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", s.getId());
                                    m.put("name", s.getTenSize());
                                    return m;
                                },
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        // Distinct colors
        List<Map<String, Object>> colors = variants.stream()
                .map(SanPhamBienThe::getMauSac)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                MauSac::getId,
                                c -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", c.getId());
                                    m.put("name", c.getMaMau());
                                    return m;
                                },
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        // Variants list
        List<Map<String, Object>> variantList = new ArrayList<>();
        for (SanPhamBienThe v : variants) {
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", v.getId());
            vm.put("sizeId", v.getSize() != null ? v.getSize().getId() : null);
            vm.put("colorId", v.getMauSac() != null ? v.getMauSac().getId() : null);
            vm.put("stock", v.getSoLuongTon());
            variantList.add(vm);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", product.getId());
        body.put("name", product.getTen());
        body.put("price", product.getGiaBan() != null ? product.getGiaBan() : BigDecimal.ZERO);
        body.put("discount", product.getKhuyenMai());
        body.put("image", image);
        body.put("description", product.getMoTa());
        body.put("sizes", sizes);
        body.put("colors", colors);
        body.put("variants", variantList);

        return ResponseEntity.ok(body);
    }
}


