package com.example.demo.repository;

import com.example.demo.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Tìm khách hàng theo Số điện thoại (Dùng khi khách đọc SĐT lúc thanh toán)
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // Kiểm tra SĐT đã tồn tại chưa
    Boolean existsByPhoneNumber(String phoneNumber);

    // Tìm kiếm khách hàng (theo Tên hoặc SĐT)
    @Query("SELECT c FROM Customer c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phoneNumber LIKE CONCAT('%', :keyword, '%'))")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);
}