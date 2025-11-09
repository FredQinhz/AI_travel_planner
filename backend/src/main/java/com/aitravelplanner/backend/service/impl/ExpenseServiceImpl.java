package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.dto.ExpenseResponse;
import com.aitravelplanner.backend.model.Expense;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.ExpenseRepository;
import com.aitravelplanner.backend.repository.TripRepository;
import com.aitravelplanner.backend.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    
    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    
    @Autowired
    public ExpenseServiceImpl(TripRepository tripRepository, ExpenseRepository expenseRepository) {
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
    }
    
    @Override
    @Transactional
    public ExpenseResponse addExpense(UUID tripId, Map<String, Object> expenseData, User user) {
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to add expenses to this trip");
        }
        
        // 创建新的消费记录
        Expense expense = new Expense();
        expense.setTrip(trip);
        expense.setUser(user);
        
        // 设置消费金额
        if (expenseData.containsKey("amount")) {
            Object amountObj = expenseData.get("amount");
            BigDecimal amount;
            if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                amount = new BigDecimal((String) amountObj);
            } else {
                throw new IllegalArgumentException("Invalid amount format");
            }
            expense.setAmount(amount);
        } else {
            throw new IllegalArgumentException("Amount is required");
        }
        
        // 设置货币类型（可选，默认为CNY）
        if (expenseData.containsKey("currency")) {
            expense.setCurrency((String) expenseData.get("currency"));
        }
        
        // 设置消费说明
        if (expenseData.containsKey("comment")) {
            expense.setComment((String) expenseData.get("comment"));
        }

        // 设置消费日期
        if (expenseData.containsKey("expenseDate")) {
            expense.setExpenseDate(LocalDate.parse((String) expenseData.get("expenseDate")));
        }

        // 设置类别
        if (expenseData.containsKey("category")) {
            expense.setCategory((String) expenseData.get("category"));
        }
        
        // 保存消费记录
        Expense savedExpense = expenseRepository.save(expense);
        
        return convertToResponse(savedExpense);
    }

    @Override
    @Transactional
    public List<ExpenseResponse> addExpensesBatch(UUID tripId, List<Map<String, Object>> expensesData, User user) {
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to add expenses to this trip");
        }
        
        // 批量创建消费记录
        List<ExpenseResponse> savedExpenses = new ArrayList<>();
        for (Map<String, Object> expenseData : expensesData) {
            Expense expense = new Expense();
            expense.setTrip(trip);
            expense.setUser(user);
            
            // 设置消费金额
            if (expenseData.containsKey("amount")) {
                Object amountObj = expenseData.get("amount");
                BigDecimal amount;
                if (amountObj instanceof Number) {
                    amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
                } else if (amountObj instanceof String) {
                    amount = new BigDecimal((String) amountObj);
                } else {
                    throw new IllegalArgumentException("Invalid amount format for one of the expenses");
                }
                expense.setAmount(amount);
            } else {
                throw new IllegalArgumentException("Amount is required for all expenses");
            }
            
            // 设置货币类型（可选，默认为CNY）
            if (expenseData.containsKey("currency")) {
                expense.setCurrency((String) expenseData.get("currency"));
            }
            
            // 设置消费说明
            if (expenseData.containsKey("comment")) {
                expense.setComment((String) expenseData.get("comment"));
            }

            // 设置消费日期
            if (expenseData.containsKey("expenseDate")) {
                expense.setExpenseDate(LocalDate.parse((String) expenseData.get("expenseDate")));
            }

            // 设置类别
            if (expenseData.containsKey("category")) {
                expense.setCategory((String) expenseData.get("category"));
            }
            
            // 保存消费记录并添加到响应列表
            savedExpenses.add(convertToResponse(expenseRepository.save(expense)));
        }
        
        return savedExpenses;
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(UUID tripId, UUID expenseId, Map<String, Object> expenseData, User user) {
        // 验证消费记录存在且属于该行程和用户
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + expenseId));
        
        // 验证消费记录属于指定行程
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified trip");
        }
        
        // 验证用户有权限修改该行程的消费记录
        if (!expense.getTrip().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this expense");
        }
        
        // 只更新用户提供的关键字段
        // 更新金额
        if (expenseData.containsKey("amount")) {
            Object amountObj = expenseData.get("amount");
            BigDecimal amount;
            if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                amount = new BigDecimal((String) amountObj);
            } else {
                throw new IllegalArgumentException("Invalid amount format");
            }
            expense.setAmount(amount);
        }
        
        // 更新其他可选字段
        if (expenseData.containsKey("currency")) {
            expense.setCurrency((String) expenseData.get("currency"));
        }
        
        if (expenseData.containsKey("comment")) {
            expense.setComment((String) expenseData.get("comment"));
        }
        
        if (expenseData.containsKey("expenseDate")) {
            expense.setExpenseDate(LocalDate.parse((String) expenseData.get("expenseDate")));
        }
        
        if (expenseData.containsKey("category")) {
            expense.setCategory((String) expenseData.get("category"));
        }
        
        // 保存更新
        Expense updatedExpense = expenseRepository.save(expense);
        
        return convertToResponse(updatedExpense);
    }

    @Override
    @Transactional
    public void deleteExpense(UUID tripId, UUID expenseId, User user) {
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete expenses for this trip");
        }
        
        // 获取消费记录并验证它属于该行程
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + expenseId));
        
        // 验证消费记录属于该行程
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified trip");
        }
        
        // 删除消费记录
        expenseRepository.delete(expense);
    }

    @Override
    public List<ExpenseResponse> getTripExpenses(UUID tripId, User user) {
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to view expenses for this trip");
        }
        
        // 查询该行程的所有消费记录并转换为响应DTO
        List<Expense> expenses = expenseRepository.findByTripId(tripId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseResponse getExpenseDetails(UUID tripId, UUID expenseId, User user) {
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to view expenses for this trip");
        }
        
        // 获取消费记录并验证它属于该行程
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + expenseId));
        
        // 验证消费记录属于该行程
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified trip");
        }
        
        return convertToResponse(expense);
    }
    
    /**
     * 将Expense实体转换为ExpenseResponse DTO
     */
    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setUserId(expense.getUser().getId());
        response.setTripId(expense.getTrip().getId());
        response.setAmount(expense.getAmount());
        response.setCurrency(expense.getCurrency());
        response.setComment(expense.getComment());
        response.setCategory(expense.getCategory());
        response.setExpenseDate(expense.getExpenseDate());
        return response;
    }
}