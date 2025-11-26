package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "Username", unique = true, nullable = false)
    private String username;

    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Column(name = "FullName",columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Status")
    private Boolean status; // true: Active, false: Locked

    // Quan hệ N-1: Nhiều User thuộc 1 Role
    @ManyToOne
    @JoinColumn(name = "RoleID")
    private Role role;
}