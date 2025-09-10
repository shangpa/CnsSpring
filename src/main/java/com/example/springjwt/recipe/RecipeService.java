package com.example.springjwt.recipe;

import com.example.springjwt.admin.dto.BoardMonthlyStatsDTO;
import com.example.springjwt.admin.dto.RecipeMonthlyStatsDTO;
import com.example.springjwt.admin.dto.RecipeStatDTO;
import com.example.springjwt.admin.enums.StatType;
import com.example.springjwt.admin.log.AdminLogService;
import com.example.springjwt.api.OpenAiService;
import com.example.springjwt.api.vision.IngredientParser;
import com.example.springjwt.fridge.Fridge;
import com.example.springjwt.fridge.FridgeRepository;
import com.example.springjwt.mypage.LikeRecipe;
import com.example.springjwt.mypage.LikeRecipeRepository;
import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.mypage.RecommendRecipeRepository;
import com.example.springjwt.point.PointActionType;
import com.example.springjwt.point.PointService;
import com.example.springjwt.recipe.cashe.IngredientNameCache;
import com.example.springjwt.review.Recipe.ReviewRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
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
    private final IngredientNameCache ingredientNameCache;
    private final IngredientParser ingredientParser;
    private final OpenAiService openAiService;
    private static final int SUGGEST_LIMIT = 3;

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
                recipes = recipeRepository.findByIsPublicTrueAndIsDraftFalseOrderByLikesDesc();
                break;
            case "latest":
                recipes = recipeRepository.findByIsPublicTrueAndIsDraftFalseOrderByCreatedAtDesc();
                break;
            case "shortTime":
                recipes = recipeRepository.findByIsPublicTrueAndIsDraftFalseOrderByCookingTimeAsc();
                break;
            case "longTime":
                recipes = recipeRepository.findByIsPublicTrueAndIsDraftFalseOrderByCookingTimeDesc();
                break;
            case "viewCount":
            default:
                recipes = recipeRepository.findByIsPublicTrueAndIsDraftFalseOrderByViewCountDesc();
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
        recipe.setUser(user); // 로그인한 유저를 레시피 작성자로 설정
        pointService.addPoint(
                user,
                PointActionType.RECIPE_WRITE,
                1,
                "레시피 작성 포인트 10점 적립"
        );
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 재료 JSON에서 재료명 파싱 → 캐시에 추가
        List<String> ingredientNames = ingredientParser.extractNames(recipeDTO.getIngredients());
        ingredientNameCache.addFromNames(ingredientNames);
        System.out.println("새 레시피 등록 시 캐시에 추가된 재료: " + ingredientNames);
        //  썸네일 자동 생성 조건:
        // - 메인이미지가 없고
        // - 조리순서(cookingSteps)가 존재할 경우만 AI 썸네일 생성
        if ((savedRecipe.getMainImageUrl() == null || savedRecipe.getMainImageUrl().isBlank())
                && savedRecipe.getCookingSteps() != null
                && !savedRecipe.getCookingSteps().trim().isEmpty()) {

            try {
                String prompt = buildPrompt(savedRecipe); // 새로 만들어줄 메서드
                String imageUrl = openAiService.generateThumbnail(prompt);

                savedRecipe.setMainImageUrl(imageUrl);
                recipeRepository.save(savedRecipe); // 다시 저장

                System.out.println("✅ AI 썸네일 생성 완료: " + imageUrl);
            } catch (Exception e) {
                System.out.println("⚠️ 썸네일 생성 실패: " + e.getMessage());
            }
        }
        return savedRecipe;
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
            // 전체 검색 → 공개 + 임시저장 제외
            recipes = recipeRepository.findByIsPublicTrueAndIsDraftFalse();
        } else {
            // 제목 검색 → 공개 + 임시저장 제외
            recipes = recipeRepository.findByTitleContainingIgnoreCaseAndIsPublicTrueAndIsDraftFalse(title);
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

                    String dateOption = matched.get(0).getDateOption();

                    result.add(new ExpectedIngredientDTO(
                            name,
                            amount,                    // 조리용 필요 수량
                            String.valueOf(totalQuantity), // 냉장고 보유량 (숫자만)
                            unitDetail,               // 단위 ("g", "ml", "개")
                            date,
                            dateOption
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
    private String buildPrompt(Recipe recipe) {
        StringBuilder prompt = new StringBuilder();

        // 제목 + 카테고리 (예: 한식 요리인 비빔밥)
        prompt.append(recipe.getCategory()).append(" 요리인 ").append(recipe.getTitle()).append("의 음식 사진입니다. ");

        // 태그 추가 (예: 매콤, 건강식)
        if (recipe.getTags() != null && !recipe.getTags().isBlank()) {
            prompt.append("이 요리는 ").append(recipe.getTags()).append(" 느낌을 줍니다. ");
        }

        // 재료 (5개까지)
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> ingredients = gson.fromJson(recipe.getIngredients(), type);
            if (ingredients != null && !ingredients.isEmpty()) {
                prompt.append("주요 재료는 ");
                prompt.append(ingredients.stream()
                        .limit(10)
                        .map(ing -> ing.get("name"))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "))
                ).append("입니다. ");
            }
        } catch (Exception ignored) {}

        // 조리 과정 요약
        try {
            Gson gson = new Gson();
            Type stepType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> steps = gson.fromJson(recipe.getCookingSteps(), stepType);
            if (steps != null && !steps.isEmpty()) {
                prompt.append("조리 순서는 다음과 같습니다: ");
                prompt.append(steps.stream()
                        .map(step -> String.valueOf(step.get("description")))
                        .collect(Collectors.joining(", "))
                ).append(". ");
            }
        } catch (Exception ignored) {}
        // 마무리 묘사
        prompt.append("이 레시피의 썸네일에 사용할 하얀색 배경에 음식 사진을 생성해주세요.");

        return prompt.toString();
    }

    //레시피 탭 - 레시피 이거 어때요?
    @Transactional(readOnly = true)
    public List<RecipeSearchResponseDTO> suggestByType(String type) {
        String regex = switch (type) {
            // 조건 1: 야식
            case "lateNightMeal" -> "(곱창|닭|치킨|닭발|피자|라면|떡볶이)";
            // 조건 2: 비오는날
            case "rainsDay"      -> "(수제비|칼국수|감자탕|전)";
            // 조건 3: 시원한
            case "cool"          -> "(초계국수|열무국수|냉면|비빔냉면|모밀)";
            // 조건 4: 이열치열
            case "heat"          -> "(삼계탕|닭죽|전골)";
            // 조건 5: 비건
            case "vegan"         -> "(비건|채식)";
            // 조건 6: 초간단
            case "superSimple"   -> "(계란찜|볶음밥|비빔밥|미역국)";
            default -> throw new IllegalArgumentException("invalid type: " + type);
        };

        // 현재 로그인(없으면 익명 허용)
        String username = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(a -> a.getName())
                .filter(n -> !"anonymousUser".equalsIgnoreCase(n))
                .orElse(null);
        UserEntity currentUser = (username != null) ? userRepository.findByUsername(username) : null;

        List<Recipe> pick = recipeRepository.findRandomPublicByRegex(regex, SUGGEST_LIMIT);

        return pick.stream().map(recipe -> {
            Double avgRatingWrapper = reviewRepository.findAvgRatingByRecipe(recipe.getRecipeId());
            double avgRating = (avgRatingWrapper != null) ? avgRatingWrapper : 0.0;
            int reviewCount = reviewRepository.countByRecipe(recipe);
            boolean liked = (currentUser != null) && likeRecipeRepository.existsByUserAndRecipe(currentUser, recipe);

            // 목록 카드용 가벼운 DTO를 쓰고 싶으면 fromEntity 쪽에서 큰 텍스트 null 처리
            return RecipeSearchResponseDTO.fromEntity(recipe, avgRating, reviewCount, liked);
        }).toList();
    }

    public RecipeDTO getMyDraftById(Long recipeId, UserEntity user) {
        Recipe recipe = recipeRepository.findByRecipeIdAndUserIdAndIsDraftTrue(recipeId, user.getId())
                .orElseThrow(() -> new RuntimeException("임시저장 레시피를 찾을 수 없습니다."));
        return RecipeDTO.fromEntity(recipe);
    }
}