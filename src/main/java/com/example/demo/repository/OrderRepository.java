package com.example.demo.repository;


import com.example.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 1. Dành cho Admin
    @Query("SELECT o FROM Order o LEFT JOIN o.customer c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +

            "(o.customer IS NULL AND (LOWER('Khách lẻ') LIKE LOWER(CONCAT('%', :search, '%')) OR 'khach le' LIKE LOWER(CONCAT('%', :search, '%')))) " +
            ")")
    Page<Order> findAllOrders(@Param("search") String search, Pageable pageable);

    // 2. Dành cho Staff
    @Query("SELECT o FROM Order o LEFT JOIN o.customer c WHERE " +
            "o.user.userId = :userId AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "(o.customer IS NULL AND (LOWER('Khách lẻ') LIKE LOWER(CONCAT('%', :search, '%')) OR 'khach le' LIKE LOWER(CONCAT('%', :search, '%')))) " +
            ")")
    Page<Order> findMyOrders(@Param("userId") Integer userId,
                             @Param("search") String search,
                             Pageable pageable);
}