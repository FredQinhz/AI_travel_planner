package com.aitravelplanner.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DayPlanDTO {
    private int day; // 第几天的计划
    private List<LocationDTO> locations; // 该天的所有位置
}