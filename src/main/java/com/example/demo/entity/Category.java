package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer categoryId;

    @Column(name = "CategoryName",columnDefinition = "NVARCHAR(255)", nullable = false)
    private String categoryName;

    @Column(name = "Description",columnDefinition = "NVARCHAR(255)")
    private String description;
}