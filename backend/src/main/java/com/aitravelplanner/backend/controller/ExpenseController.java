package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.model.Expense;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.ExpenseRepository;
import com.aitravelplanner.backend.repository.TripRepository;
import com.aitravelplanner.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class ExpenseController {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseController(TripRepository tripRepository, ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{tripId}/expenses")
    public ResponseEntity<Expense> addExpense(
            @PathVariable UUID tripId,
            @Valid @RequestBody Map<String, Object> expenseData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
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
        
        return new ResponseEntity<>(savedExpense, HttpStatus.CREATED);
    }

    // 批量上传多个消费记录
    @PostMapping("/{tripId}/expenses/batch")
    public ResponseEntity<List<Expense>> addExpensesBatch(
            @PathVariable UUID tripId,
            @Valid @RequestBody List<Map<String, Object>> expensesData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to add expenses to this trip");
        }
        
        // 批量创建消费记录
        List<Expense> savedExpenses = new ArrayList<>();
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
            
            // 保存消费记录
            savedExpenses.add(expenseRepository.save(expense));
        }
        
        return new ResponseEntity<>(savedExpenses, HttpStatus.CREATED);
    }

    // 更新单个消费记录
    @PutMapping("/{tripId}/expenses/{expenseId}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody Map<String, Object> expenseData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to update expenses for this trip");
        }
        
        // 获取消费记录并验证它属于该行程
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + expenseId));
        
        // 验证消费记录属于该行程
        if (!expense.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified trip");
        }
        
        // 更新消费记录字段
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
        
        // 保存更新后的消费记录
        Expense updatedExpense = expenseRepository.save(expense);
        
        return ResponseEntity.ok(updatedExpense);
    }

    // 删除单个消费记录
    @DeleteMapping("/{tripId}/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
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
        
        return ResponseEntity.noContent().build();
    }

    // 查询行程的所有消费记录
    @GetMapping("/{tripId}/expenses")
    public ResponseEntity<List<Expense>> getTripExpenses(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to view expenses for this trip");
        }
        
        // 查询该行程的所有消费记录
        List<Expense> expenses = expenseRepository.findByTripId(tripId);
        
        return ResponseEntity.ok(expenses);
    }

    // 查询单个消费记录详情
    @GetMapping("/{tripId}/expenses/{expenseId}")
    public ResponseEntity<Expense> getExpenseDetails(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
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
        
        return ResponseEntity.ok(expense);
    }
}