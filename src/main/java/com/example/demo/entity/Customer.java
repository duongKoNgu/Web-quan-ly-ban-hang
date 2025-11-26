package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Integer customerId;

    @Column(name = "FullName",columnDefinition = "NVARCHAR(255)", nullable = false)
    private String fullName;

    @Column(name = "PhoneNumber", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "Address",columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "Points")
    private Integer points = 0;
}