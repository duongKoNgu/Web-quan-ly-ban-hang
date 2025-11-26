package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
// import jakarta.servlet.http.Cookie; // <-- KHÔNG DÙNG CÁI NÀY NỮA
// import jakarta.servlet.http.HttpServletResponse; // <-- KHÔNG DÙNG CÁI NÀY NỮA
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // <-- Import mới
import org.springframework.http.ResponseCookie; // <-- Import mới (Quan trọng)
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("=== LOGIN REQUEST: " + loginRequest.getUsername());

        try {
            User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (user != null) {
                // --- SỬA ĐỔI QUAN TRỌNG: Dùng ResponseCookie thay vì Cookie thường ---
                ResponseCookie cookie = ResponseCookie.from("user_session", String.valueOf(user.getUserId()))
                        .httpOnly(true)  // True: JS không đọc được (An toàn hơn), False: JS đọc được
                        .secure(true)    // BẮT BUỘC TRUE khi chạy trên Railway (HTTPS)
                        .path("/")
                        .maxAge(24 * 60 * 60) // 1 ngày
                        .sameSite("None") // BẮT BUỘC NONE để Frontend Local gọi được Backend Railway
                        .build();

                System.out.println("✅ Đăng nhập thành công: " + user.getFullName());

                // Gửi Cookie qua Header "Set-Cookie"
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(user);
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
    public ResponseEntity<?> logout() {
        // Tạo cookie rỗng đè lên cookie cũ để xóa
        ResponseCookie cookie = ResponseCookie.from("user_session", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Hết hạn ngay lập tức
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Đăng xuất thành công");
    }

    /**
     * Lấy thông tin cá nhân (Profile) dựa trên Cookie
     * Endpoint: GET /user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@CookieValue(value = "user_session", defaultValue = "") String userIdStr) {
        if (userIdStr.isEmpty()) {
            return ResponseEntity.status(401).body("Chưa đăng nhập (Cookie không tìm thấy)");
        }

        return ResponseEntity.ok("User ID đang đăng nhập: " + userIdStr);
    }

    // ==================== ADMIN MANAGEMENT ====================

    /**
     * Lấy danh sách nhân viên (Chỉ Admin mới được xem)
     * Endpoint: GET /user/list
     */
    @GetMapping("/list")
    public ResponseEntity<?> listUsers(@CookieValue(value = "user_session", defaultValue = "") String userIdStr) {
        if (userIdStr.isEmpty()) return ResponseEntity.status(401).body("Chưa đăng nhập");

        try {
            Integer userId = Integer.parseInt(userIdStr);
            if (userService.checkIsAdmin(userId)) {
                List<User> users = userService.getAllUsers();
                return ResponseEntity.ok(users);
            } else {
                return ResponseEntity.status(403).body("Bạn không có quyền truy cập (Admin only)");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Lỗi xác thực user: " + e.getMessage());
        }
    }

    /**
     * Tạo nhân viên mới (Chỉ Admin mới được tạo)
     * Endpoint: POST /user/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestBody User newUser,
            @RequestParam Integer roleId,
            @CookieValue(value = "user_session", defaultValue = "") String userIdStr) {

        if (userIdStr.isEmpty()) return ResponseEntity.status(401).body("Chưa đăng nhập");

        try {
            Integer currentUserId = Integer.parseInt(userIdStr);
            if (userService.checkIsAdmin(currentUserId)) {
                User createdUser = userService.createUser(newUser, roleId);
                return ResponseEntity.ok(createdUser);
            } else {
                return ResponseEntity.status(403).body("Chỉ Admin mới được tạo nhân viên");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody User user,
            @RequestParam Integer roleId,
            @CookieValue(value = "user_session", defaultValue = "") String userIdStr) {

        if (userIdStr.isEmpty()) return ResponseEntity.status(401).body("Chưa đăng nhập");

        try {
            if (!userService.checkIsAdmin(Integer.parseInt(userIdStr))) {
                return ResponseEntity.status(403).body("Access Denied");
            }
            return ResponseEntity.ok(userService.updateUser(id, user, roleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Khóa/Mở khóa
    @PostMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleStatus(
            @PathVariable Integer id,
            @CookieValue(value = "user_session", defaultValue = "") String userIdStr) {

        if (userIdStr.isEmpty()) return ResponseEntity.status(401).body("Chưa đăng nhập");

        try {
            if (!userService.checkIsAdmin(Integer.parseInt(userIdStr))) {
                return ResponseEntity.status(403).body("Access Denied");
            }
            userService.toggleUserStatus(id);
            return ResponseEntity.ok("Đã thay đổi trạng thái user");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}