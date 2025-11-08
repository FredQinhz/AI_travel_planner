package com.aitravelplanner.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetResponse {
    private BigDecimal totalBudget;   // 总预算
    private BigDecimal spent;         // 已花费金额
    private BigDecimal remaining;     // 剩余预算
    private boolean overspend;        // 是否超支
}