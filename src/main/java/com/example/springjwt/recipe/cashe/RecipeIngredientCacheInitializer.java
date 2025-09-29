package com.example.springjwt.recipe.cashe;

import com.example.springjwt.api.vision.IngredientParser;
import com.example.springjwt.recipe.Recipe;
import com.example.springjwt.recipe.RecipeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipeIngredientCacheInitializer {

    private final RecipeRepository recipeRepository;
    private final IngredientParser ingredientParser;
    private final IngredientNameCache ingredientNameCache;

    @PostConstruct
    public void init() {
        List<Recipe> recipes = recipeRepository.findAll();
        List<String> allIngredientNames = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (recipe.getIngredients() != null) {
                List<String> names = recipe.getIngredients().stream()
                        .map(ri -> ri.getIngredient().getNameKo())
                        .toList();
                allIngredientNames.addAll(names);
            }
        }
        ingredientNameCache.initialize(allIngredientNames);
        System.out.println("✅ 재료 이름 캐시 초기화 완료: 총 " + allIngredientNames.size() + "개");
    }
}
