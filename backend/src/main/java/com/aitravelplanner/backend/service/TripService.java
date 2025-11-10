package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.TripRequest;
import com.aitravelplanner.backend.dto.TripResponse;
import com.aitravelplanner.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface TripService {
    /**
     * 创建新行程
     */
    TripResponse createTrip(TripRequest tripRequest, User user);
    
    /**
     * 获取用户的所有行程
     */
    List<TripResponse> getTripsByUser(User user);
    
    /**
     * 根据ID获取行程，并验证用户权限
     */
    TripResponse getTripById(UUID id, User user);
    
    /**
     * 更新行程
     */
    TripResponse updateTrip(UUID id, TripRequest tripRequest, User user);
    
    /**
     * 删除行程
     */
    void deleteTrip(UUID id, User user);
}
