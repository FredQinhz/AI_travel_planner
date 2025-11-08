package com.aitravelplanner.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "trips")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private BigDecimal budgetTotal;
    
    // 同行人数
    private Integer companionCount;
    
    // 旅行偏好（列表形式）
    @ElementCollection
    @CollectionTable(name = "trip_preferences")
    @Column(name = "preference")
    private List<String> preferences;
    
    // planData - 存储LLM生成的旅行计划
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String planData;
    
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
}