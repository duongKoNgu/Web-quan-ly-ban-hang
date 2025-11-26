package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Tìm theo mã SKU (Mã vạch) - Dùng khi quét mã vạch bán hàng
    Optional<Product> findBySku(String sku);

    // Tìm kiếm sản phẩm phân trang (Chỉ lấy sản phẩm chưa bị xóa)
    @Query("SELECT p FROM Product p WHERE " +
            "p.isDeleted = false AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "p.sku LIKE CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    // Lọc sản phẩm theo Danh mục
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.category.categoryId = :categoryId")
    Page<Product> findByCategory(@Param("categoryId") Integer categoryId, Pageable pageable);
}