package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Integer productId;

    @Column(name = "SKU", unique = true, nullable = false)
    private String sku; // Mã vạch

    @Column(name = "ProductName",columnDefinition = "NVARCHAR(255)", nullable = false)
    private String productName;

    // Quan trọng: Dùng BigDecimal cho tiền tệ để tránh sai số
    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    @Column(name = "StockQuantity")
    private Integer stockQuantity;

    @Column(name = "ImageURL")
    private String imageUrl;

    @Column(name = "IsDeleted")
    private Boolean isDeleted = false; // Mặc định là chưa xóa

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private Category category;
}