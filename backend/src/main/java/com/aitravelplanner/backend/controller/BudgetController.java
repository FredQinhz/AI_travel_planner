package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.dto.BudgetResponse;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.UserRepository;
import com.aitravelplanner.backend.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    public BudgetController(BudgetService budgetService, UserRepository userRepository) {
        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{tripId}/budget")
    public ResponseEntity<BudgetResponse> getBudgetStatus(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 通过用户名获取当前用户
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 调用服务获取预算状态
        BudgetResponse budgetResponse = budgetService.getBudgetStatus(tripId, user);
        
        return ResponseEntity.ok(budgetResponse);
    }
}