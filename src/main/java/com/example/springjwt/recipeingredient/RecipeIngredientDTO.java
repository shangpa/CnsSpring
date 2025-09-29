package com.example.springjwt.recipeingredient;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredientDTO {
    private Long ingredientId;   // IngredientMaster.id
    private String nameKo;       // IngredientMaster.nameKo
    private String unit;         // IngredientMaster.defaultUnit.name
    private Double quantity;     // 사용자가 입력한 수량

    /** Entity -> DTO */
    public static RecipeIngredientDTO fromEntity(RecipeIngredient ri) {
        return RecipeIngredientDTO.builder()
                .ingredientId(ri.getIngredient().getId())
                .nameKo(ri.getIngredient().getNameKo())
                .unit(ri.getIngredient().getDefaultUnit().getName())
                .quantity(ri.getQuantity())
                .build();
    }
}
