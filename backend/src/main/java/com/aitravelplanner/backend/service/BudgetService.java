package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.BudgetResponse;
import com.aitravelplanner.backend.model.User;

import java.util.UUID;

public interface BudgetService {
    /**
     * 获取行程的预算状态
     * @param tripId 行程ID
     * @param user 当前用户
     * @return 包含预算信息的响应对象
     */
    BudgetResponse getBudgetStatus(UUID tripId, User user);
}