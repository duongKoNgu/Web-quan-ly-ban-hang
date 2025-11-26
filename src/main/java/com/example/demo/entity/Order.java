package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Integer orderId;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "TotalAmount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "PaymentMethod")
    private String paymentMethod; // CASH, BANK, QR

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user; // Nhân viên bán đơn này

    @ManyToOne
    @JoinColumn(name = "CustomerID")
    private Customer customer; // Khách mua (có thể null)

    // Quan hệ 1-N: Một đơn hàng có nhiều chi tiết
    // CascadeType.ALL: Khi lưu Order, tự động lưu luôn OrderDetail
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
}