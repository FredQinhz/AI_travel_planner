package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.TripRequest;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface TripService {
    /**
     * 创建新行程，如果提供了request字段则调用LLM生成计划草案
     */
    Trip createTrip(TripRequest tripRequest, User user);
    
    /**
     * 获取用户的所有行程
     */
    List<Trip> getTripsByUser(User user);
    
    /**
     * 根据ID获取行程
     */
    Trip getTripById(UUID id);
    
    /**
     * 更新行程
     */
    Trip updateTrip(UUID id, TripRequest tripRequest, User user);
    
    /**
     * 删除行程
     */
    void deleteTrip(UUID id, User user);
}
