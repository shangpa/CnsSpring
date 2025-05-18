package com.example.springjwt.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpectedIngredientDTO {
    private String name;
    private String amountInRecipe;
    private String amountInFridge;
    private String date;
}
