package com.example.springjwt.recipe;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDTO {
    private Long recipeId;
    private String title;
    private String category;
    private String ingredients;
    private String alternativeIngredients;
    private String handlingMethods;
    private String cookingSteps;
    private String mainImageUrl;
    private String difficulty;
    private String tags;
    private int cookingTime;
    private int servings;
    private LocalDateTime createdAt;
    private boolean isPublic;
    private String writer;

    public static RecipeDTO fromEntity(Recipe recipe) {
        return RecipeDTO.builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .category(recipe.getCategory().name()) // ENUM -> String 변환
                .ingredients(recipe.getIngredients())
                .alternativeIngredients(recipe.getAlternativeIngredients())
                .handlingMethods(recipe.getHandlingMethods())
                .cookingSteps(recipe.getCookingSteps())
                .mainImageUrl(recipe.getMainImageUrl())
                .difficulty(recipe.getDifficulty().name()) // ENUM -> String 변환
                .tags(recipe.getTags())
                .cookingTime(recipe.getCookingTime())
                .servings(recipe.getServings())
                .createdAt(recipe.getCreatedAt())
                .isPublic(recipe.isPublic())
                .writer(recipe.getUser().getUsername())
                .build();
    }

    public Recipe toEntity() {
        return Recipe.builder()
                .title(this.title)
                .category(RecipeCategory.valueOf(this.category)) // String -> ENUM 변환
                .ingredients(this.ingredients)
                .alternativeIngredients(this.alternativeIngredients)
                .handlingMethods(this.handlingMethods)
                .cookingSteps(this.cookingSteps)
                .mainImageUrl(this.mainImageUrl)
                .difficulty(RecipeDifficulty.valueOf(this.difficulty)) // String -> ENUM 변환
                .tags(this.tags)
                .cookingTime(this.cookingTime)
                .servings(this.servings)
                .createdAt(this.createdAt != null ? this.createdAt : LocalDateTime.now())
                .isPublic(this.isPublic)
                .build();
    }
}