package com.example.springjwt.recipe;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUser(UserEntity user); // 특정 사용자의 레시피 조회
    List<Recipe> findByCategory(RecipeCategory category); // 특정 카테고리 레시피 조회
    List<Recipe> findByTitleContainingIgnoreCase(String title);
    @Query("SELECT r FROM Recipe r WHERE r.isPublic = true")
    List<Recipe> findAllPublicRecipes();
    List<Recipe> findByIsPublicTrue(); // 기본 공개 레시피
    List<Recipe> findByIsPublicTrueOrderByViewCountDesc();
    List<Recipe> findByIsPublicTrueOrderByLikesDesc();
    List<Recipe> findByIsPublicTrueOrderByCreatedAtDesc();
    List<Recipe> findByIsPublicTrueOrderByCookingTimeAsc();
    List<Recipe> findByIsPublicTrueOrderByCookingTimeDesc();
}
