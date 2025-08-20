package org.example.graduationproject.repositories;

import org.example.graduationproject.models.GioHang;
import org.example.graduationproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByUserAndTrangThai(User user, String trangThai);
    Optional<GioHang> findByUserIdAndTrangThai(Long userId, String trangThai);
}
