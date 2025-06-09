package com.example.springjwt.recipe;

import com.example.springjwt.admin.dto.BoardMonthlyStatsDTO;
import com.example.springjwt.admin.dto.RecipeMonthlyStatsDTO;
import com.example.springjwt.admin.dto.RecipeStatDTO;
import com.example.springjwt.admin.enums.StatType;
import com.example.springjwt.admin.log.AdminLogService;
import com.example.springjwt.fridge.Fridge;
import com.example.springjwt.fridge.FridgeRepository;
import com.example.springjwt.mypage.LikeRecipe;
import com.example.springjwt.mypage.LikeRecipeRepository;
import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.mypage.RecommendRecipeRepository;
import com.example.springjwt.point.PointActionType;
import com.example.springjwt.point.PointService;
import com.example.springjwt.review.Recipe.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  RecipeService {
    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final LikeRecipeRepository likeRecipeRepository;
    private final FridgeRepository fridgeRepository;
    private final AdminLogService adminLogService;
    private final RecommendRecipeRepository recommendRecipeRepository;

    // 전체 레시피 조회
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    // 공개된 레시피만 정렬해서 가져오기
    public List<RecipeSearchResponseDTO> getAllPublicRecipes(String sort) {
        List<Recipe> recipes;
        // 로그인한 사용자 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByUsername(username);

        switch (sort != null ? sort : "viewCount") {
            case "likes":
                recipes = recipeRepository.findByIsPublicTrueOrderByLikesDesc();
                break;
            case "latest":
                recipes = recipeRepository.findByIsPublicTrueOrderByCreatedAtDesc();
                break;
            case "shortTime":
                recipes = recipeRepository.findByIsPublicTrueOrderByCookingTimeAsc();
                break;
            case "longTime":
                recipes = recipeRepository.findByIsPublicTrueOrderByCookingTimeDesc();
                break;
            case "viewCount":
            default:
                recipes = recipeRepository.findByIsPublicTrueOrderByViewCountDesc();
                break;
        }

        return recipes.stream().map(recipe -> {
            Double avgRatingWrapper = reviewRepository.findAvgRatingByRecipe(recipe.getRecipeId());
            double avgRating = avgRatingWrapper != null ? avgRatingWrapper : 0.0;
            int reviewCount = reviewRepository.countByRecipe(recipe);
            boolean liked = likeRecipeRepository.existsByUserAndRecipe(currentUser, recipe);

            return RecipeSearchResponseDTO.fromEntity(recipe, avgRating, reviewCount, liked);
        }).collect(Collectors.toList());
    }

    // 특정 레시피 조회
    public Recipe getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다: " + id));
        recipe.setViewCount(recipe.getViewCount() + 1); // 조회수 1 증가
        recipeRepository.save(recipe); // 저장
        return recipe;
    }

    // 레시피 생성
    public Recipe createRecipe(RecipeDTO recipeDTO, String username) {
        UserEntity user = userRepository.findByUsername(username);
        Recipe recipe = recipeDTO.toEntity();
        System.out.println("로그인 된 유저 :"+user.getUsername());
        recipe.setUser(user); // 로그인한 유저를 레시피 작성자로 설정
        pointService.addPoint(
                user,
                PointActionType.RECIPE_WRITE,
                1,
                "레시피 작성 포인트 10점 적립"
        );
        return recipeRepository.save(recipe);
    }

    // 레시피 수정
    public Recipe updateRecipe(Long id, RecipeDTO recipeDTO) {
        Recipe existingRecipe = getRecipeById(id);
        existingRecipe.setTitle(recipeDTO.getTitle());
        existingRecipe.setCategory(RecipeCategory.valueOf(recipeDTO.getCategory()));
        existingRecipe.setIngredients(recipeDTO.getIngredients());
        existingRecipe.setAlternativeIngredients(recipeDTO.getAlternativeIngredients());
        existingRecipe.setCookingSteps(recipeDTO.getCookingSteps());
        existingRecipe.setMainImageUrl(recipeDTO.getMainImageUrl());
        existingRecipe.setDifficulty(RecipeDifficulty.valueOf(recipeDTO.getDifficulty()));
        existingRecipe.setCookingTime(recipeDTO.getCookingTime());
        existingRecipe.setServings(recipeDTO.getServings());
        existingRecipe.setPublic(recipeDTO.getIsPublic());
        return recipeRepository.save(existingRecipe);
    }

    // 레시피 삭제
    public void deleteRecipe(Long id) {
        Recipe recipe = getRecipeById(id);
        recipeRepository.delete(recipe);
    }

    // 레시피 검색
    public List<RecipeSearchResponseDTO> searchRecipesByTitle(String title) {
        List<Recipe> recipes;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByUsername(username);
        if (title == null || title.trim().isEmpty()) {
            recipes = recipeRepository.findAll();
        } else {
            recipes = recipeRepository.findByTitleContainingIgnoreCase(title);
        }

        return recipes.stream()
                .map(recipe -> {
                    Double avgRatingWrapper = reviewRepository.findAvgRatingByRecipe(recipe.getRecipeId());
                    double avgRating = avgRatingWrapper != null ? avgRatingWrapper : 0.0;
                    int reviewCount = reviewRepository.countByRecipe(recipe);
                    boolean liked = likeRecipeRepository.existsByUserAndRecipe(currentUser, recipe);

                    return RecipeSearchResponseDTO.fromEntity(recipe, avgRating, reviewCount, liked);
                })
                .collect(Collectors.toList());
    }

    //메인-냉장고 재료 추천 레시피
    public List<RecipeSearchResponseDTO> getRecommendedRecipesByTitleKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Recipe> recipes = recipeRepository.findByIsPublicTrue(); // 공개 레시피 전체 조회

        List<Recipe> filtered = recipes.stream()
                .filter(recipe -> keywords.stream()
                        .anyMatch(keyword -> recipe.getTitle().contains(keyword)))
                .sorted(Comparator.comparingInt(Recipe::getViewCount).reversed())
                .limit(10) // 예: 최대 10개까지만 추천
                .collect(Collectors.toList());

        return filtered.stream()
                .map(recipe -> RecipeSearchResponseDTO.fromEntity(recipe, 0.0, recipe.getLikes(), false))
                .collect(Collectors.toList());
    }

    //메인-냉장고 재료 추천 레시피 그룹
    public List<IngredientRecipeGroup> getGroupedRecommendedRecipesByTitle(List<String> keywords) {
        List<Recipe> allRecipes = recipeRepository.findByIsPublicTrue();

        return keywords.stream()
                .map(keyword -> {
                    List<Recipe> matched = allRecipes.stream()
                            .filter(recipe -> recipe.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                            .sorted(Comparator.comparingInt(Recipe::getViewCount).reversed())
                            .limit(2)
                            .collect(Collectors.toList());

                    List<RecipeSearchResponseDTO> dtos = matched.stream()
                            .map(recipe -> RecipeSearchResponseDTO.fromEntity(recipe, 0.0, recipe.getLikes(), false))
                            .collect(Collectors.toList());

                    return new IngredientRecipeGroup(keyword, dtos);
                })
                .collect(Collectors.toList());
    }

    //예상 사용 재료
    public List<ExpectedIngredientDTO> getExpectedIngredients(Long recipeId, UserEntity user) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다."));

        JSONArray ingredients = new JSONArray(recipe.getIngredients());
        List<ExpectedIngredientDTO> result = new ArrayList<>();

        for (int i = 0; i < ingredients.length(); i++) {
            JSONObject item = ingredients.getJSONObject(i);
            String name = item.optString("name", "").trim();
            String amount = item.optString("amount", "").trim();

            if (!name.isEmpty()) {
                List<Fridge> matched = fridgeRepository.findAllByUserAndIngredientNameOrderByCreatedAtAsc(user, name);

                if (!matched.isEmpty()) {
                    double totalQuantity = matched.stream()
                            .mapToDouble(Fridge::getQuantity)
                            .sum();

                    String unitDetail = matched.get(0).getUnitDetail(); // 같은 재료는 단위 동일하다고 가정

                    // 가장 오래된 fridgeDate 사용
                    String date = matched.stream()
                            .map(Fridge::getFridgeDate)
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .findFirst()
                            .orElse("날짜 없음");

                    result.add(new ExpectedIngredientDTO(
                            name,
                            amount,                    // 조리용 필요 수량
                            String.valueOf(totalQuantity), // 냉장고 보유량 (숫자만)
                            unitDetail,               // 단위 ("g", "ml", "개")
                            date
                    ));
                }
            }
        }

        return result;
    }

    // RecipeService.java
    public List<RecipeMonthlyStatsDTO> getRecentFourMonthsStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fourMonthsAgo = now.minusMonths(3).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return recipeRepository.findRecentRecipeCounts(fourMonthsAgo);
    }

    public List<BoardMonthlyStatsDTO> countRecipeMonthly(LocalDateTime startDate) {
        List<Object[]> raw = recipeRepository.countRecipeMonthlyRaw(startDate);
        return raw.stream()
                .map(row -> new BoardMonthlyStatsDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    public List<BoardMonthlyStatsDTO> sumRecipeViewsMonthly(LocalDateTime startDate) {
        List<Object[]> raw = recipeRepository.sumRecipeViewsMonthlyRaw(startDate);
        return raw.stream()
                .map(row -> new BoardMonthlyStatsDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    public List<RecipeStatDTO> getRecipeStats(StatType type, LocalDate startDate, LocalDate endDate, Integer year, Integer month) {
        if (type == StatType.YEARLY && year != null) {
            return recipeRepository.countByYear(year).stream()
                    .map(obj -> new RecipeStatDTO(obj[0] + "월", (Long) obj[1]))
                    .collect(Collectors.toList());

        } else if (type == StatType.MONTHLY && year != null && month != null) {
            return recipeRepository.countByMonth(year, month).stream()
                    .map(obj -> new RecipeStatDTO(obj[0] + "일", (Long) obj[1]))
                    .collect(Collectors.toList());

        } else if (type == StatType.DAILY && startDate != null && endDate != null) {
            return recipeRepository.countByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)).stream()
                    .map(obj -> new RecipeStatDTO(obj[0].toString(), (Long) obj[1]))
                    .collect(Collectors.toList());
        }

        throw new IllegalArgumentException("유효하지 않은 파라미터입니다.");
    }

    //관리자 - 카테고리별 통계
    public List<RecipeStatDTO> getCategoryStats() {
        List<Object[]> raw = recipeRepository.countByCategory();

        Map<RecipeCategory, Long> map = raw.stream()
                .collect(Collectors.toMap(
                        obj -> (RecipeCategory) obj[0],
                        obj -> (Long) obj[1]
                ));

        List<RecipeStatDTO> result = new ArrayList<>();
        for (RecipeCategory category : RecipeCategory.values()) {
            long count = map.getOrDefault(category, 0L);
            result.add(new RecipeStatDTO(category.name(), count));
        }

        return result;
    }

    public List<RecipeStatDTO> getMonthlyCategoryStatsByName(String category) {
        try {
            RecipeCategory enumCategory = RecipeCategory.valueOf(category);
            return recipeRepository.countMonthlyBySpecificCategory(enumCategory).stream()
                    .map(obj -> new RecipeStatDTO(obj[0].toString(), (Long) obj[1]))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }
    }

    //레시피 검색 - 제철 음식 추천
    public List<RecipeDTO> findRecipesByTitlesContaining(List<String> keywords) {
        List<Recipe> allPublic = recipeRepository.findByIsPublicTrue();

        List<Recipe> filtered = allPublic.stream()
                .filter(recipe -> keywords.stream()
                        .anyMatch(keyword -> recipe.getTitle().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());

        return filtered.stream()
                .map(RecipeDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteRecipeByAdmin(Long recipeId, String adminUsername, String reason) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피가 존재하지 않습니다."));

        // 1. 좋아요 삭제
        likeRecipeRepository.deleteAllByRecipe(recipe);

        // 2. 리뷰 삭제
        reviewRepository.deleteAllByRecipe(recipe);

        //3 추천 삭제
        recommendRecipeRepository.deleteAllByRecipe(recipe);

        // 4. 레시피 삭제
        recipeRepository.delete(recipe);

        // 5. 관리자 로그 기록
        adminLogService.logAdminAction(
                adminUsername,
                "DELETE_RECIPE",
                "RECIPE",
                recipeId,
                reason
        );
    }


}