package com.example.springjwt.recipe;

import com.example.springjwt.api.ApiUsageLimiter;
import com.example.springjwt.recipe.RecipeDTO;
import com.example.springjwt.recipe.RecipeThumbnailResponse;
import com.example.springjwt.api.OpenAiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipe")
public class RecipeThumbnailController {

    private final OpenAiService openAiService;
    private final ApiUsageLimiter apiUsageLimiter;
    private final RecipeRepository recipeRepository;

    @PostMapping("/generate-thumbnail")
    public ResponseEntity<RecipeThumbnailResponse> generateThumbnail(@RequestBody RecipeDTO recipeDTO) {
        if (!apiUsageLimiter.canUse()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new RecipeThumbnailResponse(""));
        }

        String prompt = buildPrompt(recipeDTO);
        String imageUrl = openAiService.generateThumbnail(prompt);
        return ResponseEntity.ok(new RecipeThumbnailResponse(imageUrl));
    }

    @PatchMapping("/{recipeId}/thumbnail")
    public ResponseEntity<Void> updateThumbnail(
            @PathVariable Long recipeId,
            @RequestBody Map<String, String> request
    ) {
        String imageUrl = request.get("mainImageUrl");
        System.out.println("[PATCH 요청 수신] recipeId = " + recipeId + ", imageUrl = " + imageUrl);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피 없음"));
        recipe.setMainImageUrl(imageUrl);
        recipeRepository.save(recipe);
        System.out.println("✅ [DB 저장 완료] recipeId = " + recipeId + ", 저장된 mainImageUrl = " + recipe.getMainImageUrl());
        return ResponseEntity.ok().build();
    }



    private String buildPrompt(RecipeDTO recipe) {
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

}