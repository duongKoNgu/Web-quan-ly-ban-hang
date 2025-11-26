package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Integer customerId; // Có thể null nếu là khách vãng lai
    private String customerName; // Nếu tạo mới
    private String customerPhone; // Nếu tạo mới
    private String paymentMethod; // CASH, BANK...
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Integer productId;
        private Integer quantity;
    }
}