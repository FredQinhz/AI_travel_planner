package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.DayPlanDTO;
import com.aitravelplanner.backend.model.Trip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步行程计划生成服务
 * 用于在后台异步生成LLM行程计划，不阻塞主请求
 */
@Service
@Slf4j
public class AsyncTripPlanService {

    private final LLMService llmService;
    private final LocationService locationService;

    @Autowired
    public AsyncTripPlanService(LLMService llmService, LocationService locationService) {
        this.llmService = llmService;
        this.locationService = locationService;
    }

    /**
     * 异步生成行程计划
     * 这个方法会在独立的线程中执行，不会阻塞调用者
     */
    @Async
    public void generatePlanAsync(Trip trip) {
        try {
            log.info("开始异步生成行程计划，Trip ID: {}", trip.getId());
            
            // 调用LLMService生成行程计划
            List<DayPlanDTO> dayPlans = llmService.generatePlan(trip);
            
            // 保存位置信息到数据库
            locationService.saveLocations(trip, dayPlans);
            
            log.info("行程计划生成完成，Trip ID: {}", trip.getId());
        } catch (Exception e) {
            log.error("异步生成行程计划失败，Trip ID: {}", trip.getId(), e);
            // 这里可以添加错误处理，比如发送通知或记录到数据库
        }
    }
}

