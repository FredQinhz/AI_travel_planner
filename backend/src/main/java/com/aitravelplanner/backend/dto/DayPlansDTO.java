package com.aitravelplanner.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DayPlansDTO {
    private List<DayPlanDTO> dayPlans;     // 整体行程安排
}
