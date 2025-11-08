package com.aitravelplanner.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "expenses")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private BigDecimal amount;
    private String currency = "CNY"; // Default currency
    private String comment;
    private String category; // Optional: for categorizing expenses
    private Instant createdAt = Instant.now();
}