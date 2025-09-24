package com.example.springjwt.pantry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PantryStockRepository extends JpaRepository<PantryStock, Long> {
    List<PantryStock> findAllByPantry_Id(Long pantryId);
}
