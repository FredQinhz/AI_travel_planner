package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.model.Location;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.UserRepository;
import com.aitravelplanner.backend.service.LocationService;
import com.aitravelplanner.backend.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;
    private final TripService tripService;
    private final UserRepository userRepository;

    @Autowired
    public LocationController(LocationService locationService, TripService tripService, UserRepository userRepository) {
        this.locationService = locationService;
        this.tripService = tripService;
        this.userRepository = userRepository;
    }

    /**
     * 获取指定行程的所有位置
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<List<Location>> getLocationsByTripId(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 通过用户名获取当前用户
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 验证用户是否有权限访问该行程
        try {
            tripService.getTripById(tripId, user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        List<Location> locations = locationService.findByTripId(tripId);
        return ResponseEntity.ok(locations);
    }

    /**
     * 获取指定行程某一天的位置
     */
    @GetMapping("/{tripId}/day/{day}")
    public ResponseEntity<List<Location>> getLocationsByTripIdAndDay(
            @PathVariable UUID tripId,
            @PathVariable Integer day,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 通过用户名获取当前用户
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // 验证用户是否有权限访问该行程
        try {
            tripService.getTripById(tripId, user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        List<Location> locations = locationService.findByTripIdAndDayOrderByOrderIndex(tripId, day);
        return ResponseEntity.ok(locations);
    }
}