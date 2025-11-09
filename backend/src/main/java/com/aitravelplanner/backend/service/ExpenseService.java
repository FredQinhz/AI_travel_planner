package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.ExpenseResponse;
import com.aitravelplanner.backend.model.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExpenseService {
    /**
     * 添加单个消费记录
     */
    ExpenseResponse addExpense(UUID tripId, Map<String, Object> expenseData, User user);
    
    /**
     * 批量添加消费记录
     */
    List<ExpenseResponse> addExpensesBatch(UUID tripId, List<Map<String, Object>> expensesData, User user);
    
    /**
     * 更新消费记录
     */
    ExpenseResponse updateExpense(UUID tripId, UUID expenseId, Map<String, Object> expenseData, User user);
    
    /**
     * 删除消费记录
     */
    void deleteExpense(UUID tripId, UUID expenseId, User user);
    
    /**
     * 获取行程的所有消费记录
     */
    List<ExpenseResponse> getTripExpenses(UUID tripId, User user);
    
    /**
     * 获取单个消费记录详情
     */
    ExpenseResponse getExpenseDetails(UUID tripId, UUID expenseId, User user);
}