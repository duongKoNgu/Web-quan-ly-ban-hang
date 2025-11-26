package com.example.demo.controller;

import com.example.demo.dto.OrderRequest;
import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * Tạo đơn hàng mới (Thanh toán)
     * Endpoint: POST /order/create
     * Body: JSON (OrderRequest)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest orderRequest,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        System.out.println("=== CREATE ORDER CONTROLLER ===");

        try {
            // Kiểm tra đăng nhập
            if (userIdStr == null || userIdStr.isEmpty()) {
                return ResponseEntity.status(401).body("Bạn chưa đăng nhập");
            }
            Integer userId = Integer.valueOf(userIdStr);

            Order newOrder = orderService.createOrder(orderRequest, userId);

            return ResponseEntity.ok("Tạo đơn hàng thành công! Mã đơn: " + newOrder.getOrderId());

        } catch (RuntimeException e) {
            System.err.println("❌ Lỗi nghiệp vụ: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn hàng (Phân trang & Tìm kiếm)
     * Endpoint: GET /order/list
     */
    @GetMapping("/list")
    public ResponseEntity<?> getListOrders(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        System.out.println("=== GET ORDER LIST ===");

        if (userIdStr == null || userIdStr.isEmpty()) {
            return ResponseEntity.status(401).body("Bạn chưa đăng nhập");
        }

        try {
            Integer userId = Integer.valueOf(userIdStr);

            // Lấy role của user để biết đường lọc dữ liệu (Admin hay Staff)
            // Giả sử userService có hàm lấy role, hoặc bạn tự query
            String role = userService.getUserRole(userId); // Bạn cần đảm bảo UserService có hàm này

            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

            Page<Order> orders = orderService.getOrders(userId, role, search, pageable);

            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi tải danh sách: " + e.getMessage());
        }
    }

    /**
     * Xem chi tiết đơn hàng
     * Endpoint: GET /order/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng");
        }
    }
}