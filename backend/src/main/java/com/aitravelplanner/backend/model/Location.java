package com.aitravelplanner.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

/**
 * 位置实体类，存储行程中的地点信息
 */
@Data
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;

    // 关联到行程
    @ManyToOne
    @JoinColumn(name = "trip_id")
    @JsonIgnore
    private Trip trip;

    private String name;
    private Double lng;
    private Double lat;
    private String description;
    private String type;

    // 关联字段，用来重建行程计划结构
    private Integer day;      // 第几天
    private Integer orderIndex; // 当天顺序
}