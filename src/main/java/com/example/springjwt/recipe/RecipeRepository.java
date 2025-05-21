package com.example.springjwt.recipe;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
    @Query("SELECT r FROM Recipe r WHERE r.user.id = :userId " +
            "AND (:categories IS NULL OR r.category IN :categories) " +
            "ORDER BY CASE WHEN :sort = 'views' THEN r.viewCount " +
            "WHEN :sort = 'latest' THEN r.createdAt END DESC")
    List<Recipe> findMyRecipes(
            @Param("userId") int userId,
            @Param("sort") String sort,
            @Param("categories") List<RecipeCategory> categories
    );
    // 사용자가 작성한 모든 레시피
    List<Recipe> findByUserId(int userId);
    // 사용자 ID와 레시피 ID로 단건 조회
    Optional<Recipe> findByRecipeIdAndUserId(Long recipeId, int userId);
    //메인 - 레시피 조회
    List<Recipe> findTop6ByIsPublicTrueOrderByViewCountDesc();
}
