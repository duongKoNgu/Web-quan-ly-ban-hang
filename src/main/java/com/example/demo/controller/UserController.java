package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.Cookie; // Lưu ý import đúng
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ==================== AUTHENTICATION ====================

    /**
     * Đăng nhập
     * Endpoint: POST /user/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        System.out.println("=== LOGIN REQUEST: " + loginRequest.getUsername());

        try {
            User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (user != null) {
                // 1. Tạo Cookie chứa User ID
                Cookie cookie = new Cookie("user_session", String.valueOf(user.getUserId()));
                cookie.setHttpOnly(false); // Để JS frontend đọc được (nếu cần) hoặc set true để bảo mật
                cookie.setSecure(false); // Để false nếu chạy localhost (http)
                cookie.setPath("/");
                cookie.setMaxAge(24 * 60 * 60); // 1 ngày

                // 2. Gửi Cookie về trình duyệt
                response.addCookie(cookie);

                System.out.println("✅ Đăng nhập thành công: " + user.getFullName());
                return ResponseEntity.ok(user); // Trả về thông tin user
            } else {
                return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Đăng xuất
     * Endpoint: POST /user/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Xóa cookie bằng cách set thời gian sống = 0
        Cookie cookie = new Cookie("user_session", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Đăng xuất thành công");
    }

    /**
     * Lấy thông tin cá nhân (Profile) dựa trên Cookie
     * Endpoint: GET /user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CookieValue(value = "user_session", defaultValue = "") String userIdStr) {
        if (userIdStr.isEmpty()) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        // Logic lấy User giống bài trước bạn đã làm
        // (Bạn có thể inject UserRepository vào đây để tìm User theo ID)
        return ResponseEntity.ok("User ID currently logged in: " + userIdStr);
    }

    // ==================== ADMIN MANAGEMENT ====================

    /**
     * Lấy danh sách nhân viên (Chỉ Admin mới được xem)
     * Endpoint: GET /user/list
     */
    @GetMapping("/list")
    public ResponseEntity<?> listUsers(@CookieValue(value = "user_session", defaultValue = "") String userIdStr) {
        if (userIdStr.isEmpty()) return ResponseEntity.status(401).body("Chưa đăng nhập");

        Integer userId = Integer.parseInt(userIdStr);

        // Kiểm tra quyền Admin
        if (userService.checkIsAdmin(userId)) {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(403).body("Bạn không có quyền truy cập (Admin only)");
        }
    }

    /**
     * Tạo nhân viên mới (Chỉ Admin mới được tạo)
     * Endpoint: POST /user/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestBody User newUser,
            @RequestParam Integer roleId, // Truyền roleId qua param
            @CookieValue(value = "user_session", defaultValue = "") String userIdStr) {

        if (userIdStr.isEmpty()) return ResponseEntity.status(401).body("Chưa đăng nhập");

        Integer currentUserId = Integer.parseInt(userIdStr);

        // Chỉ Admin mới được tạo người mới
        if (userService.checkIsAdmin(currentUserId)) {
            try {
                User createdUser = userService.createUser(newUser, roleId);
                return ResponseEntity.ok(createdUser);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        } else {
            return ResponseEntity.status(403).body("Chỉ Admin mới được tạo nhân viên");
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody User user,
            @RequestParam Integer roleId,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        // Check quyền Admin
        if (userIdStr == null || !userService.checkIsAdmin(Integer.parseInt(userIdStr))) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        try {
            return ResponseEntity.ok(userService.updateUser(id, user, roleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Khóa/Mở khóa
    @PostMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleStatus(
            @PathVariable Integer id,
            @CookieValue(value = "user_session", required = false) String userIdStr) {

        if (userIdStr == null || !userService.checkIsAdmin(Integer.parseInt(userIdStr))) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        userService.toggleUserStatus(id);
        return ResponseEntity.ok("Đã thay đổi trạng thái user");
    }
}