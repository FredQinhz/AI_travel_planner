package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.dto.*;
import com.aitravelplanner.backend.model.Location;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.TripRepository;
import com.aitravelplanner.backend.service.LLMService;
import com.aitravelplanner.backend.service.LocationService;
import com.aitravelplanner.backend.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final LLMService llmService;
    private final LocationService locationService;

    @Autowired
    public TripServiceImpl(TripRepository tripRepository, LLMServiceImpl llmServiceImpl, LocationService locationService) {
        this.tripRepository = tripRepository;
        this.llmService = llmServiceImpl;
        this.locationService = locationService;
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
        
        // 调用LLMService生成行程计划
        DayPlansDTO dayPlansDTO = llmService.generatePlan(savedTrip);
        
        // 保存位置信息到数据库
        locationService.saveLocations(savedTrip, dayPlansDTO);
        
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
        
        // 调用LLMService生成行程计划
        DayPlansDTO dayPlansDTO = llmService.generatePlan(updatedTrip);
        
        // 保存位置信息到数据库
        locationService.saveLocations(updatedTrip, dayPlansDTO);
        
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
        DayPlansDTO dayPlansDTO = buildDayPlansFromLocations(locations);
        response.setDayPlans(dayPlansDTO);
        
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());
        return response;
    }
    
    /**
     * 从Location列表构建DayPlansDTO
     */
    private DayPlansDTO buildDayPlansFromLocations(List<Location> locations) {
        DayPlansDTO dayPlansDTO = new DayPlansDTO();
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
        
        dayPlansDTO.setDayPlans(planData);
        return dayPlansDTO;
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
