package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.dto.TripRequest;
import com.aitravelplanner.backend.dto.TripResponse;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.UserRepository;
import com.aitravelplanner.backend.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;
    private final UserRepository userRepository;

    @Autowired
    public TripController(TripService tripService, UserRepository userRepository) {
        this.tripService = tripService;
        this.userRepository = userRepository;
    }

    /**
     * 创建新行程
     */
    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody TripRequest tripRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        Trip createdTrip = tripService.createTrip(tripRequest, user);
        return new ResponseEntity<>(convertToResponse(createdTrip), HttpStatus.CREATED);
    }

    /**
     * 获取当前用户的所有行程
     */
    @GetMapping
    public ResponseEntity<List<TripResponse>> getTrips(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        List<Trip> trips = tripService.getTripsByUser(user);
        List<TripResponse> responses = trips.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据ID获取行程
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        Trip trip = tripService.getTripById(id);
        // 验证权限
        if (!trip.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(convertToResponse(trip));
    }

    /**
     * 更新行程
     */
    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable UUID id,
            @Valid @RequestBody TripRequest tripRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        Trip updatedTrip = tripService.updateTrip(id, tripRequest, user);
        return ResponseEntity.ok(convertToResponse(updatedTrip));
    }

    /**
     * 删除行程
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        tripService.deleteTrip(id, user);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 从UserDetails中获取User实体
     */
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * 将Trip实体转换为TripResponse DTO
     */
    private TripResponse convertToResponse(Trip trip) {
        TripResponse response = new TripResponse();
        response.setId(trip.getId());
        response.setUserId(trip.getUser().getId()); // 将user实体改为userId
        response.setTitle(trip.getTitle());
        response.setDestination(trip.getDestination());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setBudgetTotal(trip.getBudgetTotal());
        response.setCompanionCount(trip.getCompanionCount());
        response.setPreferences(trip.getPreferences());
        response.setPlanData(trip.getPlanData());
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());
        return response;
    }
}