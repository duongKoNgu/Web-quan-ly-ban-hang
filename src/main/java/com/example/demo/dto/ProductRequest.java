package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRequest {
    private String sku;          // Mã vạch
    private String productName;  // Tên SP
    private Double price;        // Giá bán
    private Integer stockQuantity; // Tồn kho
    private Integer categoryId;  // ID danh mục
    private MultipartFile image; // File ảnh upload lên
}