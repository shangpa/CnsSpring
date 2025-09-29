package com.example.springjwt.fridge.recommend;
import com.example.springjwt.mypage.LikeRecipeRepository;
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
            if (isRecipeMatch(recipe, selectedIngredients)) {
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

    private boolean isRecipeMatch(Recipe recipe, List<String> selectedIngredients) {
        if (recipe.getIngredients() == null) return false;

        List<String> ingredientNames = recipe.getIngredients().stream()
                .map(ri -> ri.getIngredient().getNameKo().replaceAll("\\s+", ""))
                .toList();

        return selectedIngredients.stream()
                .map(name -> name.replaceAll("\\s+", ""))
                .allMatch(ingredientNames::contains);
    }
}