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

    @Query("SELECT new com.example.springjwt.admin.dto.UserRecipeSimpleDTO(r.recipeId, u.username, r.title, r.createdAt) " +
            "FROM Recipe r JOIN r.user u WHERE u.id = :userId")
    Page<UserRecipeSimpleDTO> findRecipesByUserId(@Param("userId") int userId, Pageable pageable);


    @Query("SELECT new com.example.springjwt.admin.dto.RecipeListAdminDTO(r.recipeId, r.user.username, r.title, r.createdAt) " +
            "FROM Recipe r ORDER BY r.createdAt DESC")
    Page<RecipeListAdminDTO> findAllForAdmin(Pageable pageable);

    @Query("SELECT new com.example.springjwt.admin.dto.RecipeListAdminDTO(r.recipeId, r.user.username, r.title, r.createdAt) " +
            "FROM Recipe r " +
            "WHERE r.title LIKE %:title% " +
            "ORDER BY r.createdAt DESC")
    Page<RecipeListAdminDTO> searchByTitleForAdmin(@Param("title") String title, Pageable pageable);

    @Query("SELECT new com.example.springjwt.admin.dto.UserRecipeSimpleDTO(r.recipeId,r.user.username, r.title, r.createdAt) " +
            "FROM Recipe r WHERE r.user.id = :userId AND r.title LIKE %:keyword% ORDER BY r.createdAt DESC")
    Page<UserRecipeSimpleDTO> findRecipesByUserIdAndTitleContains(@Param("userId") int userId,
                                                                  @Param("keyword") String keyword,
                                                                  Pageable pageable);

    // 연도별 - 월 단위 집계
    @Query("SELECT MONTH(r.createdAt), COUNT(r) FROM Recipe r WHERE YEAR(r.createdAt) = :year GROUP BY MONTH(r.createdAt)")
    List<Object[]> countByYear(@Param("year") int year);

    // 월별 - 일 단위 집계
    @Query("SELECT DAY(r.createdAt), COUNT(r) FROM Recipe r WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month GROUP BY DAY(r.createdAt)")
    List<Object[]> countByMonth(@Param("year") int year, @Param("month") int month);

    // 기간별 - 날짜 단위 집계
    @Query("SELECT DATE(r.createdAt), COUNT(r) FROM Recipe r WHERE r.createdAt BETWEEN :start AND :end GROUP BY DATE(r.createdAt)")
    List<Object[]> countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //카테고리 통계
    @Query("SELECT r.category, COUNT(r) FROM Recipe r GROUP BY r.category")
    List<Object[]> countByCategory();

    @Query("""
    SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m'), COUNT(r)
    FROM Recipe r
    WHERE r.category = :category
    GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    """)
    List<Object[]> countMonthlyBySpecificCategory(@Param("category") RecipeCategory category);


    //레시피 검색 - 제철 음식 추천
    List<Recipe> findByTitleInAndIsPublicTrue(List<String> titles);

    //레시피 탭 - 레시피 이거 어때요?
    @Query(value = """
        SELECT * 
        FROM recipe r
        WHERE r.isPublic = 1
          AND CONCAT_WS(' ', r.title, r.tags) REGEXP :regex
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Recipe> findRandomPublicByRegex(@Param("regex") String regex, @Param("limit") int limit);
}
