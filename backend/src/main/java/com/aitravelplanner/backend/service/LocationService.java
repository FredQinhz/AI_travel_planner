package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.DayPlanDTO;
import com.aitravelplanner.backend.model.Location;
import com.aitravelplanner.backend.model.Trip;

import java.util.List;
import java.util.UUID;

/**
 * Location服务接口
 */
public interface LocationService {
    
    /**
     * 根据行程ID查找所有位置
     */
    List<Location> findByTripId(UUID tripId);
    
    /**
     * 保存行程的位置信息
     */
    void saveLocations(Trip trip, List<DayPlanDTO> dayPlans);
    
    /**
     * 删除行程相关的所有位置
     */
    void deleteByTripId(UUID tripId);
    
    /**
     * 根据行程ID和天数获取当天的位置，按顺序排序
     */
    List<Location> findByTripIdAndDayOrderByOrderIndex(UUID tripId, Integer day);
}