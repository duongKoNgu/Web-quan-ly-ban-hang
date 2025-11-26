package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    // --- 1. Logic Lấy quyền (Đã làm) ---
    public String getUserRole(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        System.out.println(user.getRole());
        // Kiểm tra kỹ từng lớp để tránh NullPointerException
        if (user != null && user.getRole() != null) {

            return user.getRole().getRoleName();
        }
        return ""; // Trả về chuỗi rỗng thay vì null để tránh lỗi ở Controller
    }

    public boolean checkIsAdmin(Integer userId) {
        return "Admin".equalsIgnoreCase(getUserRole(userId));
    }

    // --- 2. Logic Đăng nhập ---
    public User login(String username, String password) {
        // Tìm user theo username
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // So sánh password (Lưu ý: Thực tế nên dùng BCrypt để mã hóa)
            if (user.getPasswordHash().equals(password)) {
                if (!user.getStatus()) {
                    throw new RuntimeException("Tài khoản đã bị khóa!");
                }
                return user; // Đăng nhập thành công
            }
        }
        return null; // Sai tên đăng nhập hoặc mật khẩu
    }

    // --- 3. Logic Tạo nhân viên mới (Cho Admin) ---
    public User createUser(User newUser, Integer roleId) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        newUser.setRole(role);
        newUser.setStatus(true); // Mặc định hoạt động

        return userRepository.save(newUser);
    }

    // --- 4. Lấy tất cả nhân viên ---
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Integer id, User req, Integer roleId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setFullName(req.getFullName());
        user.setPhoneNumber(req.getPhoneNumber());

        // Nếu có gửi mật khẩu mới thì cập nhật (Không gửi thì giữ nguyên)
        if (req.getPasswordHash() != null && !req.getPasswordHash().isEmpty()) {
            user.setPasswordHash(req.getPasswordHash());
        }

        // Cập nhật Role
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role sai"));
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    // --- KHÓA / MỞ KHÓA TÀI KHOẢN (Thay vì xóa) ---
    public void toggleUserStatus(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Đảo ngược trạng thái (Đang mở -> Khóa, Đang khóa -> Mở)
        // Lưu ý: check null status
        boolean currentStatus = user.getStatus() != null && user.getStatus();
        user.setStatus(!currentStatus);

        userRepository.save(user);
    }
}