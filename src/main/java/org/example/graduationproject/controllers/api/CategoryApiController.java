package org.example.graduationproject.controllers.api;

import org.example.graduationproject.models.Loai;
import org.example.graduationproject.repositories.LoaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CategoryApiController {

    @Autowired
    private LoaiRepository loaiRepository;

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            List<Loai> categories = loaiRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách danh mục thành công");
            response.put("categories", categories);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi lấy danh sách danh mục: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
