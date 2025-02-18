package com.example.springjwt.recipe;

import com.example.springjwt.User.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recipeId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;  // 레시피 작성자 (외래 키)

    private String title;

    @Enumerated(EnumType.STRING)
    private RecipeCategory category; // 카테고리 (ENUM)

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ingredients; // JSON 형식의 재료

    @Lob
    @Column(columnDefinition = "TEXT")
    private String alternativeIngredients; // JSON 형식의 대체 재료

    @Lob
    @Column(columnDefinition = "TEXT")
    private String cookingSteps; // JSON 형식의 조리 순서

    private String mainImageUrl; // 대표 사진 URL

    @Enumerated(EnumType.STRING)
    private RecipeDifficulty difficulty; // 난이도 (ENUM)

    private int cookingTime; // 소요시간 (분 단위)
    private int servings; // 인원수
    private LocalDateTime createdAt; // 생성 일시
    private boolean isPublic; // 공개 여부
}