package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.dto.ExpenseResponse;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.UserRepository;
import com.aitravelplanner.backend.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class ExpenseController {

    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public ExpenseController(UserRepository userRepository, ExpenseService expenseService) {
        this.userRepository = userRepository;
        this.expenseService = expenseService;
    }

    // 添加单个消费记录
    @PostMapping("/{tripId}/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable UUID tripId,
            @Valid @RequestBody Map<String, Object> expenseData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ExpenseResponse expenseResponse = expenseService.addExpense(tripId, expenseData, user);
        return new ResponseEntity<>(expenseResponse, HttpStatus.CREATED);
    }

    // 批量上传多个消费记录
    @PostMapping("/{tripId}/expenses/batch")
    public ResponseEntity<List<ExpenseResponse>> addExpensesBatch(
            @PathVariable UUID tripId,
            @Valid @RequestBody List<Map<String, Object>> expensesData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<ExpenseResponse> savedExpenses = expenseService.addExpensesBatch(tripId, expensesData, user);
        return new ResponseEntity<>(savedExpenses, HttpStatus.CREATED);
    }

    // 更新单个消费记录 - 支持部分字段更新
    @PutMapping("/{tripId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            @RequestBody Map<String, Object> expenseData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 获取当前用户
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ExpenseResponse updatedExpense = expenseService.updateExpense(tripId, expenseId, expenseData, user);
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
        
        expenseService.deleteExpense(tripId, expenseId, user);
        return ResponseEntity.noContent().build();
    }

    // 查询行程的所有消费记录
    @GetMapping("/{tripId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getTripExpenses(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<ExpenseResponse> responses = expenseService.getTripExpenses(tripId, user);
        return ResponseEntity.ok(responses);
    }

    // 查询单个消费记录详情
    @GetMapping("/{tripId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpenseDetails(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ExpenseResponse expenseResponse = expenseService.getExpenseDetails(tripId, expenseId, user);
        return ResponseEntity.ok(expenseResponse);
    }
}