package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.dto.TripRequest;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.TripRepository;
import com.aitravelplanner.backend.service.LLMService;
import com.aitravelplanner.backend.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final LLMService llmService;

    @Autowired
    public TripServiceImpl(TripRepository tripRepository, LLMService llmService) {
        this.tripRepository = tripRepository;
        this.llmService = llmService;
    }

    @Override
    @Transactional
    public Trip createTrip(TripRequest tripRequest, User user) {
        // 验证日期合法性
        if (tripRequest.getEndDate().isBefore(tripRequest.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        // 创建Trip对象
        Trip trip = new Trip();
        trip.setUser(user);
        trip.setTitle(tripRequest.getTitle());
        trip.setDestination(tripRequest.getDestination());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        trip.setBudgetTotal(BigDecimal.valueOf(tripRequest.getBudgetTotal()));
        trip.setCompanionCount(tripRequest.getCompanionCount());
        trip.setPreferences(tripRequest.getPreferences());
        trip.setCreatedAt(Instant.now());
        trip.setUpdatedAt(Instant.now());


        // 如果提供了request字段，则调用LLM生成计划草案并存储到planData字段
        if (tripRequest.getRequest() != null && !tripRequest.getRequest().isEmpty()) {
            try {
                String planJson = llmService.generatePlan(
                        tripRequest.getDestination(),
                        tripRequest.getStartDate(),
                        tripRequest.getEndDate(),
                        tripRequest.getBudgetTotal(),
                        tripRequest.getRequest()
                );
                trip.setPlanData(planJson); // 存储到planData字段
            } catch (Exception e) {
                // 如果LLM调用失败，仍创建trip并在planData中写入错误信息
                trip.setPlanData("{\"error\":\"Failed to generate plan: "
                        + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        }

        return tripRepository.save(trip);
    }

    @Override
    public List<Trip> getTripsByUser(User user) {
        return tripRepository.findByUser(user);
    }

    @Override
    public Trip getTripById(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + id));
    }

    @Override
    @Transactional
    public Trip updateTrip(UUID id, TripRequest tripRequest, User user) {
        Trip trip = getTripById(id);
        
        // 验证权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this trip");
        }

        // 验证日期合法性
        if (tripRequest.getEndDate().isBefore(tripRequest.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        // 更新字段
        trip.setTitle(tripRequest.getTitle());
        trip.setDestination(tripRequest.getDestination());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        trip.setBudgetTotal(BigDecimal.valueOf(tripRequest.getBudgetTotal()));
        trip.setUpdatedAt(Instant.now());
        
        // 更新新添加的字段
        trip.setCompanionCount(tripRequest.getCompanionCount());
        trip.setPreferences(tripRequest.getPreferences());

        // 如果提供了request字段，则重新生成计划并存储到planData字段
        if (tripRequest.getRequest() != null && !tripRequest.getRequest().isEmpty()) {
            try {
                String planJson = llmService.generatePlan(
                        tripRequest.getDestination(),
                        tripRequest.getStartDate(),
                        tripRequest.getEndDate(),
                        tripRequest.getBudgetTotal(),
                        tripRequest.getRequest()
                );
                trip.setPlanData(planJson); // 存储到planData字段
            } catch (Exception e) {
                trip.setPlanData("{\"error\":\"Failed to generate plan: "
                        + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        }

        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void deleteTrip(UUID id, User user) {
        Trip trip = getTripById(id);
        
        // 验证权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this trip");
        }

        tripRepository.delete(trip);
    }
}
