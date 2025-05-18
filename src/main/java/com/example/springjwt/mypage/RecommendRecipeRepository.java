package com.example.springjwt.mypage;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendRecipeRepository extends JpaRepository<RecommendRecipe, Long> {
    Optional<RecommendRecipe> findByUserAndRecipe(UserEntity user, Recipe recipe);
    boolean existsByUserAndRecipe(UserEntity user, Recipe recipe);
}
