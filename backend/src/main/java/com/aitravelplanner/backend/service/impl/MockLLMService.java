package com.aitravelplanner.backend.service.impl;

import org.springframework.stereotype.Service;

import com.aitravelplanner.backend.dto.DayPlanDTO;
import com.aitravelplanner.backend.dto.DayPlansDTO;
import com.aitravelplanner.backend.dto.LocationDTO;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.service.LLMService;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟LLM服务实现类
 */
@Service
public class MockLLMService implements LLMService {

    @Override
    public DayPlansDTO generatePlan(Trip trip) {
        DayPlansDTO dayPlansDTO = new DayPlansDTO();
        List<DayPlanDTO> planData = new ArrayList<>();
        dayPlansDTO.setPlanData(planData);
        
        // 假设行程有2天
        int days = 2;
        for (int day = 1; day <= days; day++) {
            DayPlanDTO dayPlanDTO = new DayPlanDTO();
            dayPlanDTO.setDay(day);
            List<LocationDTO> locations = new ArrayList<>();
            dayPlanDTO.setLocations(locations);
            
            // 第一天的景点
            if (day == 1) {
                // 主要景点
                LocationDTO attraction = new LocationDTO();
                attraction.setName(trip.getDestination() + " 主要景点");
                attraction.setLng(generateRandomLng());
                attraction.setLat(generateRandomLat());
                attraction.setDescription("这是" + trip.getDestination() + "最著名的景点之一");
                attraction.setType("attraction");
                locations.add(attraction);
                
                // 特色餐厅
                LocationDTO restaurant = new LocationDTO();
                restaurant.setName(trip.getDestination() + " 特色餐厅");
                restaurant.setLng(generateRandomLng());
                restaurant.setLat(generateRandomLat());
                restaurant.setDescription("当地著名的餐厅，提供特色美食");
                restaurant.setType("restaurant");
                locations.add(restaurant);
            } else {
                // 第二天的景点
                LocationDTO cultural = new LocationDTO();
                cultural.setName(trip.getDestination() + " 文化景点");
                cultural.setLng(generateRandomLng());
                cultural.setLat(generateRandomLat());
                cultural.setDescription("展示当地丰富文化历史的景点");
                cultural.setType("cultural");
                locations.add(cultural);
                
                LocationDTO shopping = new LocationDTO();
                shopping.setName(trip.getDestination() + " 购物中心");
                shopping.setLng(generateRandomLng());
                shopping.setLat(generateRandomLat());
                shopping.setDescription("购买纪念品的好地方");
                shopping.setType("shopping");
                locations.add(shopping);
            }
            
            planData.add(dayPlanDTO);
        }
        
        return dayPlansDTO;
    }
    
    /**
     * 生成随机经度（-180到180之间）
     */
    private double generateRandomLng() {
        return -180 + (Math.random() * 360);
    }
    
    /**
     * 生成随机纬度（-90到90之间）
     */
    private double generateRandomLat() {
        return -90 + (Math.random() * 180);
    }
    
}
