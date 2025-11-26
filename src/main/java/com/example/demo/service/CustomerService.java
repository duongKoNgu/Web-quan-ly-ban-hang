package com.example.demo.service;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // --- LẤY DANH SÁCH (Tìm kiếm) ---
    public Page<Customer> getCustomers(String keyword, Pageable pageable) {
        return customerRepository.searchCustomers(keyword, pageable);
    }

    // --- THÊM MỚI ---
    public Customer createCustomer(Customer req) {
        // Validate SĐT
        if (customerRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại " + req.getPhoneNumber() + " đã tồn tại trong hệ thống!");
        }

        // Mặc định điểm = 0 nếu null
        if (req.getPoints() == null) req.setPoints(0);

        return customerRepository.save(req);
    }

    // --- CẬP NHẬT ---
    public Customer updateCustomer(Integer id, Customer req) {
        Customer current = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // Kiểm tra nếu đổi SĐT thì SĐT mới có trùng ai không
        if (!current.getPhoneNumber().equals(req.getPhoneNumber()) &&
                customerRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại mới đã được sử dụng bởi người khác!");
        }

        current.setFullName(req.getFullName());
        current.setPhoneNumber(req.getPhoneNumber());
        current.setAddress(req.getAddress());
        // Không cho phép sửa điểm thủ công ở đây (Điểm chỉ thay đổi khi mua hàng), hoặc cho phép tùy Admin

        return customerRepository.save(current);
    }

    // --- XÓA ---
    public void deleteCustomer(Integer id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Khách hàng không tồn tại");
        }
        customerRepository.deleteById(id);
    }
}