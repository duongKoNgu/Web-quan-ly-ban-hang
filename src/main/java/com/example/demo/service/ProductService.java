package com.example.demo.service;

import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Đường dẫn lưu ảnh (Tạo thư mục 'uploads/products' trong project)
    private final Path uploadDir = Paths.get("uploads/products");

    public ProductService() {
        // Tạo thư mục nếu chưa có
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload", e);
        }
    }

    // ==================== CREATE PRODUCT ====================
    public Product createProduct(ProductRequest req) throws IOException {
        // 1. Kiểm tra SKU trùng
        if (productRepository.findBySku(req.getSku()).isPresent()) {
            throw new RuntimeException("Mã SKU " + req.getSku() + " đã tồn tại!");
        }

        // 2. Tìm danh mục
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // 3. Map dữ liệu
        Product product = new Product();
        product.setSku(req.getSku());
        product.setProductName(req.getProductName());
        product.setPrice(BigDecimal.valueOf(req.getPrice())); // Convert Double -> BigDecimal
        product.setStockQuantity(req.getStockQuantity());
        product.setCategory(category);
        product.setIsDeleted(false);

        // 4. Xử lý lưu ảnh
        if (req.getImage() != null && !req.getImage().isEmpty()) {
            String fileName = saveImage(req.getImage());
            product.setImageUrl(fileName);
        }

        return productRepository.save(product);
    }

    // ==================== UPDATE PRODUCT ====================
    public Product updateProduct(Integer id, ProductRequest req) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Cập nhật thông tin cơ bản
        product.setProductName(req.getProductName());
        product.setPrice(BigDecimal.valueOf(req.getPrice()));
        product.setStockQuantity(req.getStockQuantity());

        // Cập nhật danh mục
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        product.setCategory(category);

        // Cập nhật ảnh (chỉ khi user gửi ảnh mới)
        if (req.getImage() != null && !req.getImage().isEmpty()) {
            // (Optional) Xóa ảnh cũ nếu cần
            String fileName = saveImage(req.getImage());
            product.setImageUrl(fileName);
        }

        return productRepository.save(product);
    }

    // ==================== DELETE (SOFT DELETE) ====================
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        product.setIsDeleted(true); // Đánh dấu là đã xóa
        productRepository.save(product);
    }

    // ==================== HELPER: SAVE FILE ====================
    private String saveImage(MultipartFile file) throws IOException {
        // Tạo tên file ngẫu nhiên để tránh trùng (VD: asd12-image.png)
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Copy file vào thư mục
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }

        return fileName;
    }

    // ==================== GET LIST (SEARCH) ====================
    public Page<Product> getProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}