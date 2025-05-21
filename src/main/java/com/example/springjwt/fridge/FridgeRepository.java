package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {
    //조회기준
    List<Fridge> findByUserIdOrderByUpdatedAtDesc(Long userId);
    long countByUser(UserEntity user);
    Optional<Fridge> findByUserAndIngredientName(UserEntity user, String ingredientName);
    Optional<Fridge> findTopByUserAndIngredientNameOrderByUpdatedAtDesc(UserEntity user, String ingredientName);
}
