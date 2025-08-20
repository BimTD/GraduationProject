package org.example.graduationproject.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, columnDefinition = "NVARCHAR(255)")
    private String username;

    @Column(name = "password", columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "enabled", columnDefinition = "NVARCHAR(10)")
    private String enabled;

    @Column(name = "email", unique = true, columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "provider", columnDefinition = "NVARCHAR(50)")
    private String provider;

    @Column(name = "ho_ten", columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "so_dien_thoai", columnDefinition = "NVARCHAR(20)")
    private String soDienThoai;

    @Column(name = "dia_chi", columnDefinition = "NVARCHAR(500)")
    private String diaChi;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserRole> userRoles;
}
