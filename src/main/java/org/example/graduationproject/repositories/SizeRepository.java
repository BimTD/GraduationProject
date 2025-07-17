package org.example.graduationproject.repositories;

import org.example.graduationproject.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    List<Size> findByLoai_Id(Integer loaiId);
} 