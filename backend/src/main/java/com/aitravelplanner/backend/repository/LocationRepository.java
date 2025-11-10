package com.aitravelplanner.backend.repository;

import com.aitravelplanner.backend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Location实体的数据库访问接口
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    
    /**
     * 根据行程ID查找所有位置
     */
    List<Location> findByTripId(UUID tripId);
    
    /**
     * 根据行程ID和天数查找位置，按顺序索引排序
     */
    List<Location> findByTripIdAndDayOrderByOrderIndex(UUID tripId, Integer day);
    
    /**
     * 删除行程相关的所有位置
     */
    void deleteByTripId(UUID tripId);
}