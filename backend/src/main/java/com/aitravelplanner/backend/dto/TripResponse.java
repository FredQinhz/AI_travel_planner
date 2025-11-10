package com.aitravelplanner.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class TripResponse {
    private UUID id;
    private UUID userId;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budgetTotal;
    private Integer companionCount;
    private List<String> preferences;
    private List<DayPlanDTO> dayPlans;
    private Instant createdAt;
    private Instant updatedAt;
}