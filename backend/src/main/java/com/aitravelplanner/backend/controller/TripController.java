package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.dto.TripRequest;
import com.aitravelplanner.backend.dto.TripResponse;
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
        TripResponse tripResponse = tripService.createTrip(tripRequest, user);
        return new ResponseEntity<>(tripResponse, HttpStatus.CREATED);
    }

    /**
     * 获取用户的所有行程
     */
    @GetMapping
    public ResponseEntity<List<TripResponse>> getTrips(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        List<TripResponse> responses = tripService.getTripsByUser(user);
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据ID获取行程
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        TripResponse tripResponse = tripService.getTripById(id, user);
        return ResponseEntity.ok(tripResponse);
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
        TripResponse tripResponse = tripService.updateTrip(id, tripRequest, user);
        return ResponseEntity.ok(tripResponse);
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
}