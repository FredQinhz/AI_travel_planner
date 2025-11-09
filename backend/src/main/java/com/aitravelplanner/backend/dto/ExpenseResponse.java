package com.aitravelplanner.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 消费记录响应DTO
 * 将Expense实体转换为API响应格式，避免返回完整实体对象
 */
@Data
public class ExpenseResponse {
    private UUID id;
    private UUID userId;
    private UUID tripId;
    private String comment;
    private BigDecimal amount;
    private String currency;
    private String category;
    private LocalDate expenseDate;
}