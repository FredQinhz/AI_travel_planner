package com.aitravelplanner.backend.repository;

import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.TripVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripVersionRepository extends JpaRepository<TripVersion, UUID> {
    List<TripVersion> findByTrip(Trip trip);
    List<TripVersion> findByTripId(UUID tripId);
    List<TripVersion> findByTripIdOrderByVersionDesc(UUID tripId);
    TripVersion findByTripIdAndVersion(UUID tripId, Integer version);
    Integer countByTripId(UUID tripId);
}