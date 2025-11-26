package com.example.demo.service;

import com.example.demo.dto.OrderRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // ==================== CREATE ORDER (CHECKOUT) ====================

    @Transactional(rollbackFor = Exception.class) // Quan trọng: Lỗi là rollback lại hết
    public Order createOrder(OrderRequest request, Integer userId) {
        System.out.println("=== SERVICE: createOrder ===");
        System.out.println("Staff ID: " + userId);

        // 1. Lấy thông tin Nhân viên bán hàng
        User staff = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại ID: " + userId));

        // 2. Xử lý thông tin Khách hàng
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
        } else if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            // Logic tạo khách hàng mới nhanh nếu chưa có
            customer = new Customer();
            customer.setFullName(request.getCustomerName());
            customer.setPhoneNumber(request.getCustomerPhone());
            customer = customerRepository.save(customer);
            System.out.println("✅ Đã tạo khách hàng mới: " + customer.getFullName());
        }

        // 3. Khởi tạo Đơn hàng
        Order order = new Order();
        order.setUser(staff);
        order.setCustomer(customer);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderDetails(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. Duyệt qua từng sản phẩm trong giỏ hàng
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại ID: " + itemReq.getProductId()));

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getProductName() + " không đủ hàng. Tồn: " + product.getStockQuantity());
            }

            // TRỪ KHO
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product); // Cập nhật kho

            // Tạo chi tiết đơn hàng
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(product.getPrice()); // Lưu giá tại thời điểm bán

            // Tính tổng tiền
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            order.getOrderDetails().add(detail);
        }

        order.setTotalAmount(totalAmount);

        // 5. Lưu đơn hàng xuống DB (Cascade sẽ tự lưu OrderDetails)
        Order savedOrder = orderRepository.save(order);

        // Cộng điểm cho khách nếu có (Ví dụ: 100k = 1 điểm)
        if (customer != null) {
            int pointsToAdd = totalAmount.intValue() / 100000;
            customer.setPoints(customer.getPoints() + pointsToAdd);
            customerRepository.save(customer);
        }

        System.out.println("✅ Order created successfully ID: " + savedOrder.getOrderId());
        return savedOrder;
    }

    // ==================== GET ORDERS ====================

    public Page<Order> getOrders(Integer userId, String role, String search, Pageable pageable) {
        if ("Admin".equalsIgnoreCase(role)) {
            // Admin xem hết
            return orderRepository.findAllOrders(search, pageable);
        } else {
            // Staff chỉ xem đơn của mình
            return orderRepository.findMyOrders(userId, search, pageable);
        }
    }

    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found ID: " + orderId));
    }
}