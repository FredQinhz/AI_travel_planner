package com.aitravelplanner.backend.repository;

import com.aitravelplanner.backend.model.Expense;
import com.aitravelplanner.backend.model.Trip;
import com.aitravelplanner.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByTrip(Trip trip);
    List<Expense> findByTripId(UUID tripId);
    List<Expense> findByUser(User user);
    List<Expense> findByUserId(UUID userId);
    List<Expense> findByTripIdAndUserId(UUID tripId, UUID userId);
}