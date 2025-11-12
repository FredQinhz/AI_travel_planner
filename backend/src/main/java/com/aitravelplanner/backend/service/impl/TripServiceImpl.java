package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.dto.*;
import com.aitravelplanner.backend.model.Location;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.TripRepository;
import com.aitravelplanner.backend.repository.ExpenseRepository;
import com.aitravelplanner.backend.service.LocationService;
import com.aitravelplanner.backend.service.ExpenseService;
import com.aitravelplanner.backend.service.TripService;
import com.aitravelplanner.backend.service.AsyncTripPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final AsyncTripPlanService asyncTripPlanService;
    private final LocationService locationService;
    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;

    @Autowired
    public TripServiceImpl(TripRepository tripRepository, AsyncTripPlanService asyncTripPlanService, LocationService locationService, ExpenseService expenseService, ExpenseRepository expenseRepository) {
        this.tripRepository = tripRepository;
        this.asyncTripPlanService = asyncTripPlanService;
        this.locationService = locationService;
        this.expenseService = expenseService;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional
    public TripResponse createTrip(TripRequest tripRequest, User user) {
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

        Trip savedTrip = tripRepository.save(trip);
        
        // 异步调用LLMService生成行程计划（不阻塞响应）
        // 通过独立的异步服务类调用，确保@Async注解生效
        asyncTripPlanService.generatePlanAsync(savedTrip);
        
        // 立即返回响应，不等待LLM处理完成
        // dayPlans字段会是空的，但前端会提示用户稍后查看
        return convertToResponse(savedTrip);
    }

    @Override
    public List<TripResponse> getTripsByUser(User user) {
        List<Trip> trips = tripRepository.findByUser(user);
        return trips.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public TripResponse getTripById(UUID id, User user) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + id));
        
        // 验证权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to access this trip");
        }
        
        return convertToResponse(trip);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(UUID id, TripRequest tripRequest, User user) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + id));
        
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

        Trip updatedTrip = tripRepository.save(trip);
        
        // 异步调用LLMService重新生成行程计划（不阻塞响应）
        asyncTripPlanService.generatePlanAsync(updatedTrip);
        
        // 立即返回响应，不等待LLM处理完成
        return convertToResponse(updatedTrip);
    }

    @Override
    @Transactional
    public void deleteTrip(UUID id, User user) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + id));
        
        // 验证权限
        if (!trip.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this trip");
        }

        // 级联删除顺序：
        // 1. 删除locations表中的相关记录
        locationService.deleteByTripId(id);
        
        // 2. 删除expenses表中的相关记录
        expenseRepository.deleteByTripId(id);
        
        // 3. 删除trips表中的记录（删除Trip时会自动级联删除trip_preferences表中的记录）
        tripRepository.delete(trip);
    }
    
    /**
     * 将Trip实体转换为TripResponse DTO
     */
    private TripResponse convertToResponse(Trip trip) {
        TripResponse response = new TripResponse();
        response.setId(trip.getId());
        response.setUserId(trip.getUser().getId());
        response.setTitle(trip.getTitle());
        response.setDestination(trip.getDestination());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setBudgetTotal(trip.getBudgetTotal());
        response.setCompanionCount(trip.getCompanionCount());
        response.setPreferences(trip.getPreferences());
        
        // 从数据库中查询位置信息并构建行程计划
        List<Location> locations = locationService.findByTripId(trip.getId());
        List<DayPlanDTO> dayPlans = buildDayPlansFromLocations(locations);
        response.setDayPlans(dayPlans);
        
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());
        return response;
    }
    
    /**
     * 从Location列表构建DayPlanDTO列表
     */
    private List<DayPlanDTO> buildDayPlansFromLocations(List<Location> locations) {
        List<DayPlanDTO> planData = new ArrayList<>();
        
        // 按天分组
        Map<Integer, List<Location>> locationsByDay = locations.stream()
                .collect(Collectors.groupingBy(Location::getDay));
        
        // 遍历每一天
        for (Integer day : locationsByDay.keySet().stream().sorted().collect(Collectors.toList())) {
            DayPlanDTO dayPlanDTO = new DayPlanDTO();
            dayPlanDTO.setDay(day);
            
            // 按顺序索引排序位置
            List<LocationDTO> locationDTOs = locationsByDay.get(day).stream()
                    .sorted(Comparator.comparing(Location::getOrderIndex))
                    .map(this::convertToLocationDTO)
                    .collect(Collectors.toList());
            
            dayPlanDTO.setLocations(locationDTOs);
            planData.add(dayPlanDTO);
        }
        
        return planData;
    }
    
    /**
     * 将Location实体转换为LocationDTO
     */
    private LocationDTO convertToLocationDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setName(location.getName());
        dto.setLng(location.getLng());
        dto.setLat(location.getLat());
        dto.setDescription(location.getDescription());
        dto.setType(location.getType());
        return dto;
    }
    
}
