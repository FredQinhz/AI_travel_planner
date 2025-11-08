package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.dto.BudgetResponse;
import com.aitravelplanner.backend.model.Expense;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.ExpenseRepository;
import com.aitravelplanner.backend.repository.TripRepository;
import com.aitravelplanner.backend.service.BudgetService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetServiceImpl(TripRepository tripRepository, ExpenseRepository expenseRepository) {
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public BudgetResponse getBudgetStatus(UUID tripId, User user) {
        // 获取行程并验证权限
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));
        
        // 验证用户权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to access this trip's budget");
        }
        
        // 获取行程的所有消费记录
        List<Expense> expenses = expenseRepository.findByTripId(tripId);
        
        // 计算已花费金额
        BigDecimal spent = expenses.stream()
                .map(Expense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算剩余预算和是否超支
        BigDecimal totalBudget = trip.getBudgetTotal() != null ? trip.getBudgetTotal() : BigDecimal.ZERO;
        BigDecimal remaining = totalBudget.subtract(spent);
        boolean overspend = remaining.compareTo(BigDecimal.ZERO) < 0;
        
        // 创建并返回响应
        BudgetResponse response = new BudgetResponse();
        response.setTotalBudget(totalBudget);
        response.setSpent(spent);
        response.setRemaining(remaining);
        response.setOverspend(overspend);
        
        return response;
    }
}