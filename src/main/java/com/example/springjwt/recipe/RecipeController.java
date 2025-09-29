package com.example.springjwt.recipe;

import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.ingredient.IngredientMaster;
import com.example.springjwt.ingredient.IngredientMasterRepository;
import com.example.springjwt.recipeingredient.RecipeIngredient;
import com.example.springjwt.recipeingredient.RecipeIngredientRepository;
import com.example.springjwt.search.SearchKeywordService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import com.example.springjwt.search.RecipeSearchService;
import com.example.springjwt.search.SortKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final SearchKeywordService searchKeywordService;
    private final RecipeRepository recipeRepository;
    private final RecipeSearchService recipeSearchService;
    private final IngredientMasterRepository ingredientMasterRepository;

    // 레시피 전체 조회
    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        List<RecipeDTO> recipes = recipeService.getAllRecipes()
                .stream()
                .map(RecipeDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recipes);
    }

    // 공개 레시피 조회 (정렬 옵션)
    @GetMapping("/public")
    public List<RecipeSearchResponseDTO> getPublicRecipes(
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return recipeService.getAllPublicRecipes(sort);
    }

    // 특정 레시피 조회 (+조회수 증가)
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(RecipeDTO.fromEntity(recipe));
    }

    // 레시피 생성(발행)
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @RequestBody RecipeDTO recipeDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Recipe recipe = recipeService.createRecipe(recipeDTO, userDetails.getUsername());
        RecipeResponseDTO response = new RecipeResponseDTO(
                true,
                "레시피가 성공적으로 생성되었습니다.",
                recipe.getRecipeId()
        );
        return ResponseEntity.ok(response);
    }

    // 레시피 수정(발행 상태)
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> updateRecipe(
            @PathVariable Long id,
            @RequestBody RecipeDTO recipeDTO
    ) {
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDTO);
        return ResponseEntity.ok(RecipeDTO.fromEntity(updatedRecipe));
    }

    // 레시피 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // 프론트가 호출하는 검색 (리스트)
    @GetMapping("/search")
    public List<Recipe> searchRecipes(
            @RequestParam String title,
            @RequestParam(required = false) String category,   // "koreaFood" 등
            @RequestParam(required = false) String sort        // viewCount|likes|latest|shortTime|longTime
    ) {
        if (title != null && !title.isBlank()) {
            searchKeywordService.saveKeyword(title);          // 인기 검색어 저장
        }
        SortKey key = parseSort(sort);
        Page<Recipe> page = recipeSearchService.search(title, category, key, 0, 200);
        return page.getContent();
    }

    // ✅ 페이징 버전 (선택)
    @GetMapping("/search/page")
    public Page<Recipe> searchRecipesPaged(
            @RequestParam String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (title != null && !title.isBlank()) {
            searchKeywordService.saveKeyword(title);
        }
        SortKey key = parseSort(sort);
        return recipeSearchService.search(title, category, key, page, size);
    }

    private SortKey parseSort(String sort) {
        if (sort == null || sort.isBlank()) return SortKey.latest;
        try { return SortKey.valueOf(sort); } catch (IllegalArgumentException e) { return SortKey.latest; }
    }

    // 메인 - 냉장고 재료 추천 레시피
    @GetMapping("/recommend-by-title")
    public ResponseEntity<List<RecipeSearchResponseDTO>> recommendByTitle(
            @RequestParam List<String> ingredients
    ) {
        List<RecipeSearchResponseDTO> recommended =
                recipeService.getRecommendedRecipesByTitleKeywords(ingredients);
        return ResponseEntity.ok(recommended);
    }

    // 메인 - 냉장고 재료 추천 레시피 그룹
    @PostMapping("/recommend-grouped")
    public ResponseEntity<List<IngredientRecipeGroup>> recommendGroupedByTitle(
            @RequestBody List<String> ingredients
    ) {
        List<IngredientRecipeGroup> result =
                recipeService.getGroupedRecommendedRecipesByTitle(ingredients);
        return ResponseEntity.ok(result);
    }

    // 예상 사용 재료
    @GetMapping("/{recipeId}/expected-ingredients")
    public ResponseEntity<List<ExpectedIngredientDTO>> getExpectedIngredients(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ExpectedIngredientDTO> list =
                recipeService.getExpectedIngredients(recipeId, userDetails.getUserEntity());
        return ResponseEntity.ok(list);
    }

    // 메인 - 레시피 조회 TOP6
    @GetMapping("/top/view")
    public ResponseEntity<List<RecipeSearchResponseDTO>> getTopViewedRecipes() {
        List<Recipe> top = recipeRepository.findTop6ByIsPublicTrueOrderByViewCountDesc();
        List<RecipeSearchResponseDTO> result = top.stream()
                .map(r -> RecipeSearchResponseDTO.fromEntity(r, 0.0, 0, false))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // 제철 음식 추천 (제목 기준)
    @GetMapping("/seasonal")
    public List<RecipeDTO> getSeasonalRecipes() {
        List<String> seasonalTitles = List.of("삼계탕", "초계국수", "콩국수", "물회", "오이냉국");
        return recipeService.findRecipesByTitlesContaining(seasonalTitles);
    }

    // 레시피 탭 - 레시피 이거 어때요?
    @GetMapping("/suggest")
    public ResponseEntity<List<RecipeSearchResponseDTO>> suggest(@RequestParam String type) {
        List<RecipeSearchResponseDTO> list = recipeService.suggestByType(type);
        return ResponseEntity.ok(list);
    }

    // ===================== 임시저장(Draft) API =====================

    // 내 임시저장 레시피 리스트
    @GetMapping("/drafts")
    public ResponseEntity<List<RecipeDTO>> getMyDrafts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Recipe> drafts =
                recipeRepository.findByUserIdAndIsDraftTrueOrderByCreatedAtDesc(
                        Long.valueOf(userDetails.getUserEntity().getId())
                );
        List<RecipeDTO> result = drafts.stream()
                .map(RecipeDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // 임시저장 단건 조회(소유자 검증)
    @GetMapping("/drafts/{recipeId}")
    public ResponseEntity<RecipeDTO> getMyDraftById(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecipeDTO draft = recipeService.getMyDraftById(recipeId, userDetails.getUserEntity());
        return ResponseEntity.ok(draft);
    }

    // 임시저장 생성
    @PostMapping("/drafts")
    public ResponseEntity<RecipeResponseDTO> createDraft(
            @RequestBody RecipeDTO dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("Draft create DTO: {}", dto); // OK
        Recipe entity = dto.toEntityDraftSafe();
        entity.setUser(user.getUserEntity());
        entity.setDraft(true);
        entity.setPublic(false);
        entity.setCreatedAt(LocalDateTime.now());

        Recipe saved = recipeRepository.save(entity);
        return ResponseEntity.ok(new RecipeResponseDTO(true, "임시저장 생성", saved.getRecipeId()));
    }

    // 임시저장 수정(작성 중 계속 저장)
    @PutMapping("/drafts/{id}")
    public ResponseEntity<RecipeDTO> updateDraft(
            @PathVariable Long id,
            @RequestBody RecipeDTO dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Recipe draft = recipeRepository
                .findByRecipeIdAndUserIdAndIsDraftTrue(id, user.getUserEntity().getId())
                .orElseThrow(() -> new RuntimeException("임시저장이 없거나 권한 없음."));

        if (StringUtils.hasText(dto.getTitle())) draft.setTitle(dto.getTitle());
        if (StringUtils.hasText(dto.getCategory())) {
            try { draft.setCategory(RecipeCategory.valueOf(dto.getCategory())); }
            catch (Exception e) { log.warn("Invalid category: {}", dto.getCategory()); }
        }
        if (dto.getIngredients() != null) {
            List<RecipeIngredient> ingList = dto.getIngredients().stream()
                    .map(riDto -> RecipeIngredient.builder()
                            .recipe(draft)
                            .ingredient(ingredientMasterRepository.findById(riDto.getIngredientId())
                                    .orElseThrow(() -> new IllegalArgumentException("재료 없음: " + riDto.getIngredientId())))
                            .quantity(riDto.getQuantity())
                            .build())
                    .toList();

            draft.getIngredients().clear();
            draft.getIngredients().addAll(ingList);
        }

        if (StringUtils.hasText(dto.getAlternativeIngredients())) draft.setAlternativeIngredients(dto.getAlternativeIngredients());
        if (StringUtils.hasText(dto.getHandlingMethods())) draft.setHandlingMethods(dto.getHandlingMethods());
        if (StringUtils.hasText(dto.getCookingSteps())) draft.setCookingSteps(dto.getCookingSteps());
        if (StringUtils.hasText(dto.getMainImageUrl())) draft.setMainImageUrl(dto.getMainImageUrl());
        if (StringUtils.hasText(dto.getDifficulty())) {
            try { draft.setDifficulty(RecipeDifficulty.valueOf(dto.getDifficulty())); }
            catch (Exception e) { log.warn("Invalid difficulty: {}", dto.getDifficulty()); }
        }
        if (StringUtils.hasText(dto.getTags())) draft.setTags(dto.getTags());
        if (dto.getCookingTime() != null) draft.setCookingTime(dto.getCookingTime());
        if (dto.getServings()    != null) draft.setServings(dto.getServings());
        if (StringUtils.hasText(dto.getVideoUrl())) draft.setVideoUrl(dto.getVideoUrl());
        if (StringUtils.hasText(dto.getRecipeType())) {
            try { draft.setRecipeType(RecipeType.valueOf(dto.getRecipeType())); }
            catch (Exception e) { log.warn("Invalid recipeType: {}", dto.getRecipeType()); }
        }

        draft.setDraft(true);
        draft.setPublic(false);

        Recipe saved = recipeRepository.save(draft);
        return ResponseEntity.ok(RecipeDTO.fromEntity(saved));
    }

    // 임시저장 삭제
    @DeleteMapping("/drafts/{id}")
    public ResponseEntity<Void> deleteMyDraft(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Recipe draft = recipeRepository
                .findByRecipeIdAndUserIdAndIsDraftTrue(id, user.getUserEntity().getId())
                .orElseThrow(() -> new RuntimeException("임시저장이 없거나 권한 없음."));
        recipeRepository.delete(draft);
        return ResponseEntity.noContent().build();
    }

    // 임시저장을 발행(공개/비공개)으로 전환
    @PostMapping("/{id}/publish")
    public ResponseEntity<RecipeDTO> publish(
            @PathVariable Long id,
            @RequestBody PublishRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Recipe draft = recipeRepository
                .findByRecipeIdAndUserIdAndIsDraftTrue(id, user.getUserEntity().getId())
                .orElseThrow(() -> new RuntimeException("임시저장이 없거나 권한 없음."));

        // 필수 검증(필요 시 추가)
        if (draft.getTitle() == null || draft.getCategory() == null) {
            throw new IllegalArgumentException("제목/카테고리는 필수입니다.");
        }

        draft.setDraft(false); // 발행
        draft.setPublic(Boolean.TRUE.equals(req.getIsPublic()));

        Recipe saved = recipeRepository.save(draft);
        return ResponseEntity.ok(RecipeDTO.fromEntity(saved));
    }

    // 발행 요청 바디
    @Getter
    @Setter
    public static class PublishRequest {
        private Boolean isPublic; // true/false
    }
}
