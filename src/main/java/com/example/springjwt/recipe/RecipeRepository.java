package com.example.springjwt.recipe;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.admin.dto.RecipeListAdminDTO;
import com.example.springjwt.admin.dto.RecipeMonthlyStatsDTO;
import com.example.springjwt.admin.dto.UserRecipeSimpleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    List<Recipe> findTop3ByIsPublicTrueOrderByViewCountDesc();//얘는 3개임

    // RecipeRepository.java
    @Query("SELECT new com.example.springjwt.admin.dto.RecipeMonthlyStatsDTO(CAST(FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m') AS string), COUNT(r)) " +
            "FROM Recipe r " +
            "WHERE r.createdAt >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')")
    List<RecipeMonthlyStatsDTO> findRecentRecipeCounts(@Param("startDate") LocalDateTime startDate);

    // 월별 레시피 개수
    @Query("""
    SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m'), COUNT(r)
    FROM Recipe r
    WHERE r.createdAt >= :startDate
    GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
""")
    List<Object[]> countRecipeMonthlyRaw(@Param("startDate") LocalDateTime startDate);

    // 월별 총 조회수
    @Query("""
    SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m'), SUM(r.viewCount)
    FROM Recipe r
    WHERE r.createdAt >= :startDate
    GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
""")
    List<Object[]> sumRecipeViewsMonthlyRaw(@Param("startDate") LocalDateTime startDate);

    int countByUser(UserEntity user);

    @Query("SELECT new com.example.springjwt.admin.dto.UserRecipeSimpleDTO(r.user.username, r.title, r.createdAt) " +
            "FROM Recipe r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<UserRecipeSimpleDTO> findRecipesByUserId(int userId);

    @Query("SELECT new com.example.springjwt.admin.dto.RecipeListAdminDTO(r.recipeId, r.user.username, r.title, r.createdAt) " +
            "FROM Recipe r ORDER BY r.createdAt DESC")
    Page<RecipeListAdminDTO> findAllForAdmin(Pageable pageable);

    @Query("SELECT new com.example.springjwt.admin.dto.RecipeListAdminDTO(r.recipeId, r.user.username, r.title, r.createdAt) " +
            "FROM Recipe r " +
            "WHERE r.title LIKE %:title% " +
            "ORDER BY r.createdAt DESC")
    Page<RecipeListAdminDTO> searchByTitleForAdmin(@Param("title") String title, Pageable pageable);

}
