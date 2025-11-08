package com.aitravelplanner.backend.model;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    private Instant createdAt = Instant.now();
    
    private Instant updatedAt;
}