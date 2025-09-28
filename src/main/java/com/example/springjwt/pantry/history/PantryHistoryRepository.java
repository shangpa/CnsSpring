package com.example.springjwt.pantry.history;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PantryHistoryRepository extends JpaRepository<PantryHistory, Long> {

    @EntityGraph(attributePaths = {"ingredient","unit","stock"})
    List<PantryHistory> findByPantry_IdOrderByCreatedAtDesc(Long pantryId);

    @EntityGraph(attributePaths = {"ingredient","unit","stock"})
    List<PantryHistory> findByPantry_IdAndIngredient_NameKoContainingIgnoreCaseOrderByCreatedAtDesc(
            Long pantryId, String keyword
    );
}