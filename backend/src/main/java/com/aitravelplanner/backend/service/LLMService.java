package com.aitravelplanner.backend.service;

import java.time.LocalDate;

public interface LLMService {
    /**
     * 根据结构化请求生成旅行计划草案
     * @param destination 目的地
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param budgetTotal 总预算
     * @param request 原始用户请求文本
     * @return JSON字符串格式的旅行计划草案
     */
    String generatePlan(String destination, LocalDate startDate, LocalDate endDate, Double budgetTotal, String request);
}
