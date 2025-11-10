package com.aitravelplanner.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitravelplanner.backend.dto.DayPlanDTO;
import com.aitravelplanner.backend.dto.LocationDTO;
import com.aitravelplanner.backend.model.Location;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.repository.LocationRepository;
import com.aitravelplanner.backend.service.LocationService;

import java.util.List;
import java.util.UUID;

/**
 * Location服务实现类
 */
@Service
public class LocationServiceImpl implements LocationService {
    
    private final LocationRepository locationRepository;
    
    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }
    
    @Override
    public List<Location> findByTripId(UUID tripId) {
        return locationRepository.findByTripId(tripId);
    }
    
    @Override
    public void saveLocations(Trip trip, List<DayPlanDTO> dayPlans) {
        try {
            // 首先删除该行程已有的位置信息
            locationRepository.deleteByTripId(trip.getId());
            
            if (dayPlans != null) {
                for (DayPlanDTO dayPlanDTO : dayPlans) {
                    int day = dayPlanDTO.getDay();
                    int order = 1;
                    
                    if (dayPlanDTO.getLocations() != null) {
                        for (LocationDTO locationDTO : dayPlanDTO.getLocations()) {
                            Location entity = new Location();
                            entity.setTrip(trip);
                            entity.setName(locationDTO.getName());
                            entity.setLng(locationDTO.getLng());
                            entity.setLat(locationDTO.getLat());
                            entity.setDescription(locationDTO.getDescription());
                            entity.setType(locationDTO.getType());
                            
                            // 设置关键字段
                            entity.setDay(day);
                            entity.setOrderIndex(order++);
                            
                            locationRepository.save(entity);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 记录异常，但不影响主流程
            e.printStackTrace();
        }
    }
    
    @Override
    public void deleteByTripId(UUID tripId) {
        locationRepository.deleteByTripId(tripId);
    }
    
    @Override
    public List<Location> findByTripIdAndDayOrderByOrderIndex(UUID tripId, Integer day) {
        return locationRepository.findByTripIdAndDayOrderByOrderIndex(tripId, day);
    }
}