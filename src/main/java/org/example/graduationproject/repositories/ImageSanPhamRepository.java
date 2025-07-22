package org.example.graduationproject.repositories;

import org.example.graduationproject.models.ImageSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageSanPhamRepository extends JpaRepository<ImageSanPham, Integer> {
    java.util.List<ImageSanPham> findAllBySanPham_Id(Integer sanPhamId);
} 