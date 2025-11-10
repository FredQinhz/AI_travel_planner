package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.dto.DayPlansDTO;
import com.aitravelplanner.backend.model.Trip;

public interface LLMService {
    /**
     * 根据结构化请求生成旅行计划草案
     * @param trip 行程对象，包含目的地、开始日期、结束日期、总预算和原始用户请求文本
     * @return 行程计划数据传输对象
     */
    DayPlansDTO generatePlan(Trip trip);
}
