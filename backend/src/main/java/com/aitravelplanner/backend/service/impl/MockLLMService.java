package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.service.LLMService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class MockLLMService implements LLMService {

    @Override
    public String generatePlan(String destination, LocalDate startDate, LocalDate endDate, Double budgetTotal, String request) {
        // 返回一个结构良好的旅行计划JSON，适合存储在planData字段中
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"destination\":\"").append(destination).append("\",");
        json.append("\"startDate\":\"").append(startDate.toString()).append("\",");
        json.append("\"endDate\":\"").append(endDate.toString()).append("\",");
        json.append("\"budgetTotal\":").append(budgetTotal).append(",");
        json.append("\"request\":\"").append(escapeJson(request)).append("\",");
        json.append("\"days\":[");
        
        // 计算旅行天数
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        // 添加每日计划
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            json.append("{");
            json.append("\"date\":\"").append(currentDate.toString()).append("\",");
            json.append("\"activities\":[");
            
            // 为每天添加一些模拟活动
            json.append("{");
            json.append("\"time\":\"09:00\",");
            json.append("\"description\":\"早餐\",");
            json.append("\"location\":\"酒店餐厅\"");
            json.append("},");
            
            json.append("{");
            json.append("\"time\":\"10:00\",");
            json.append("\"description\":\"游览当地景点\",");
            json.append("\"location\":\"").append(destination).append("主要景点\"");
            json.append("},");
            
            json.append("{");
            json.append("\"time\":\"18:00\",");
            json.append("\"description\":\"晚餐\",");
            json.append("\"location\":\"当地餐厅\"");
            json.append("}");
            
            json.append("]");
            json.append("}");
            
            if (i < days - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        json.append("}");
        
        return json.toString();
    }
    
    // 辅助方法：转义JSON字符串中的特殊字符
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
