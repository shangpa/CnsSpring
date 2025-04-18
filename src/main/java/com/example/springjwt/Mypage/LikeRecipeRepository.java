package com.example.springjwt.Mypage;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRecipeRepository extends JpaRepository<LikeRecipe, Long> {
    boolean existsByUserAndRecipe(UserEntity user, Recipe recipe);
    Optional<LikeRecipe> findByUserAndRecipe(UserEntity user, Recipe recipe);
    List<LikeRecipe> findByUser(UserEntity user);
    Long countByRecipe(Recipe recipe);

    void delete(LikeRecipe likeRecipe);
}
