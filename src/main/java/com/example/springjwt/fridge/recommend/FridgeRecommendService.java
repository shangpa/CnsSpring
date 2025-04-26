package com.example.springjwt.fridge.recommend;
import com.example.springjwt.Mypage.LikeRecipeRepository;
import com.example.springjwt.recipe.Recipe;
import com.example.springjwt.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FridgeRecommendService {

    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final ObjectMapper objectMapper;

    public List<RecipeRecommendResponseDTO> recommendRecipes(List<String> selectedIngredients) {
        List<Recipe> allRecipes = recipeRepository.findAll();
        List<RecipeRecommendResponseDTO> result = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (isRecipeMatch(recipe.getIngredients(), selectedIngredients)) {
                int likeCount = likeRecipeRepository.countByRecipe(recipe);
                RecipeRecommendResponseDTO dto = RecipeRecommendResponseDTO.builder()
                        .recipeId(recipe.getRecipeId())
                        .title(recipe.getTitle())
                        .mainImageUrl(recipe.getMainImageUrl())
                        .difficulty(recipe.getDifficulty() != null ? recipe.getDifficulty().name() : null)
                        .cookingTime(recipe.getCookingTime())
                        .reviewAverage(0.0) // 리뷰 평균(추후)
                        .reviewCount(0)     // 리뷰 수(추후)
                        .writerNickname(recipe.getUser().getUsername())
                        .viewCount(recipe.getViewCount())
                        .likeCount(likeCount)
                        .createdAt(recipe.getCreatedAt().toString())
                        .build();
                result.add(dto);
            }
        }

        return result;
    }

    private boolean isRecipeMatch(String ingredientsJson, List<String> selectedIngredients) {
        try {
            JsonNode ingredientsNode = objectMapper.readTree(ingredientsJson);
            List<String> ingredientNames = new ArrayList<>();

            for (JsonNode ingredient : ingredientsNode) {
                String name = ingredient.get("name").asText().replaceAll("\\s+", ""); // 공백 제거
                ingredientNames.add(name);
            }

            // 선택한 재료도 공백 제거해서 비교
            return selectedIngredients.stream()
                    .map(name -> name.replaceAll("\\s+", "")) // 공백 제거
                    .allMatch(ingredientNames::contains);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}