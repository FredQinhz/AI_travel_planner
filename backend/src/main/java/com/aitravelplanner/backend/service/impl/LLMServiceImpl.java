package com.aitravelplanner.backend.service.impl;

import com.aitravelplanner.backend.dto.DayPlanDTO;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.service.LLMService;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Primary
@Slf4j
public class LLMServiceImpl implements LLMService {

    @Value("${llm.qwen.apiKey:your_api_key_here}")
    private String apiKey;

    @Value("${llm.qwen.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<DayPlanDTO> generatePlan(Trip trip) {
        try {
            String prompt = buildPrompt(trip);

            Generation gen = new Generation();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(Collections.singletonList(userMsg))
                    .build();

            GenerationResult result = gen.call(param);
            String json = result.getOutput().getText();

            log.info("LLM 返回 JSON: {}", json);

            // 解析JSON，直接提取dayPlans数组
            JsonNode rootNode = objectMapper.readTree(json);
            return objectMapper.readValue(rootNode.get("dayPlans").traverse(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, DayPlanDTO.class));

        } catch (Exception e) {
            log.error("调用通义千问失败", e);
            throw new RuntimeException("LLM 生成失败: " + e.getMessage());
        }
    }

    private String buildPrompt(Trip trip) {
        return "你是一个专业旅行规划 AI，用户将提供目的地、天数、预算、人数以及旅行偏好，请根据以下 JSON 生成多日旅行计划，包括交通、住宿、景点、餐厅等详细信息。\n\n" +
               "输入 Trip 信息：\n" +
               tripToJson(trip) + "\n\n" +
               "要求：\n" +
               "1. 生成 dayPlans 数组，每天包含多个 location\n" +
               "2. location 必须包含 name, lng, lat, description, type\n" +
               "3. 只返回 JSON，不要包含 markdown 或额外解释\n" +
               "4. 务必做到 location 的经纬度信息准确\n" +
               "5. 必须严格按格式返回\n\n" +
               "你必须返回如下结构：\n" +
               "{\n" +
               "  \"dayPlans\": [\n" +
               "    {\n" +
               "      \"day\": 1,\n" +
               "      \"locations\": [\n" +
               "        {\n" +
               "          \"name\": \"浅草寺\",\n" +
               "          \"lng\": 139.7967,\n" +
               "          \"lat\": 35.7148,\n" +
               "          \"description\": \"历史古寺\",\n" +
               "          \"type\": \"景点\"\n" +
               "        }\n" +
               "      ]\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }

    private String tripToJson(Trip trip) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("title", trip.getTitle());
            node.put("destination", trip.getDestination());
            node.put("startDate", trip.getStartDate().toString());
            node.put("endDate", trip.getEndDate().toString());
            node.put("budgetTotal", trip.getBudgetTotal());
            node.put("companionCount", trip.getCompanionCount());
            node.set("preferences", objectMapper.valueToTree(trip.getPreferences()));
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}