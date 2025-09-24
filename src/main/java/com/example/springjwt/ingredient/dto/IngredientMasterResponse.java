package com.example.springjwt.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class IngredientMasterResponse {
    private Long id;
    private String nameKo;
    private String category;     // enum name()
    private Long defaultUnitId;  // nullable
    private String iconUrl;      // nullable
}