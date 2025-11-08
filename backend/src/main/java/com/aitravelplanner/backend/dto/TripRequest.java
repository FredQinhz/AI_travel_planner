package com.aitravelplanner.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.*;

@Data
public class TripRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "目的地不能为空")
    private String destination;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    @NotNull(message = "预算不能为空")
    @Min(value = 0, message = "预算不能为负数")
    private Double budgetTotal;
    
    // 同行人数
    @Min(value = 0, message = "同行人数不能为负数")
    private Integer companionCount = 0;
    
    // 旅行偏好
    private List<String> preferences;
    
    private String request; // 用户原始自然语言请求
}