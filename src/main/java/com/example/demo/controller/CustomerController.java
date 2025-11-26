package com.example.demo.controller;

import com.example.demo.entity.Customer;
import com.example.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")

public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // 1. Lấy danh sách & Tìm kiếm
    @GetMapping("/list")
    public ResponseEntity<?> getList(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        if (userIdStr == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customers = customerService.getCustomers(search, pageable);
        return ResponseEntity.ok(customers);
    }

    // 2. Thêm mới
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Customer customer,
                                    @CookieValue(value = "user_session", required = false) String userIdStr) {
        try {
            if (userIdStr == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
            return ResponseEntity.ok(customerService.createCustomer(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Cập nhật
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody Customer customer,
                                    @CookieValue(value = "user_session", required = false) String userIdStr) {
        try {
            if (userIdStr == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
            return ResponseEntity.ok(customerService.updateCustomer(id, customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Xóa
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @CookieValue(value = "user_session", required = false) String userIdStr) {
        try {
            if (userIdStr == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
            customerService.deleteCustomer(id);
            return ResponseEntity.ok("Đã xóa khách hàng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}