package com.aitravelplanner.backend.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private String name;          // 景点名称
    private Double lng;           // 经度
    private Double lat;           // 纬度
    private String description;   // 简介
    private String type;          // 类型
}