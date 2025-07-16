package org.example.graduationproject.repositories;

import org.example.graduationproject.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
} 