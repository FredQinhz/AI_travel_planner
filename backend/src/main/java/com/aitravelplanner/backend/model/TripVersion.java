package com.aitravelplanner.backend.model;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "trip_versions")
@Data
public class TripVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
    
    private Integer version;
    
    @Column(columnDefinition = "jsonb")
    private String data; // JSON string for the trip data at this version
    
    private String description; // Optional description for this version
    private Instant createdAt = Instant.now();
}