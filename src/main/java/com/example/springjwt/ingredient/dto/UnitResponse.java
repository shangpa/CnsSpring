package com.example.springjwt.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class UnitResponse {
    private Long id;
    private String name;  // g, kg, ml, L, 개 …
    private String kind;  // WEIGHT/VOLUME/COUNT
}