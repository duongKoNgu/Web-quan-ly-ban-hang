package com.example.demo.controller;

import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController

@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // ==================== CREATE ====================
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(
            @ModelAttribute ProductRequest productRequest, // Dùng ModelAttribute để nhận File + Data
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        System.out.println("Cookie userIdStr nhận được: " + userIdStr);

        try {
            // 1. Check quyền Admin
            if (userIdStr == null || !userService.checkIsAdmin(Integer.valueOf(userIdStr))) {
                return ResponseEntity.status(403).body("Chỉ Admin mới được thêm sản phẩm");
            }

            // 2. Gọi Service
            Product newProduct = productService.createProduct(productRequest);
            return ResponseEntity.ok("Thêm sản phẩm thành công: " + newProduct.getProductName());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // ==================== UPDATE ====================
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @ModelAttribute ProductRequest productRequest,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        try {
            if (userIdStr == null || !userService.checkIsAdmin(Integer.valueOf(userIdStr))) {
                return ResponseEntity.status(403).body("Access Denied");
            }

            Product updated = productService.updateProduct(id, productRequest);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== DELETE ====================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Integer id,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        try {
            if (userIdStr == null || !userService.checkIsAdmin(Integer.valueOf(userIdStr))) {
                return ResponseEntity.status(403).body("Access Denied");
            }

            productService.deleteProduct(id);
            return ResponseEntity.ok("Đã xóa sản phẩm (Soft Delete)");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== READ LIST ====================
    @GetMapping("/list")
    public ResponseEntity<?> getList(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getProducts(search, pageable);
        return ResponseEntity.ok(products);
    }

    // ==================== GET IMAGE ====================
    // API này giúp Frontend hiển thị ảnh: <img src="http://localhost:8080/product/images/ten-anh.jpg" />
    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            Path file = Paths.get("uploads/products").resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Hoặc IMAGE_PNG tùy file
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}