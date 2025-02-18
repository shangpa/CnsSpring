package com.example.springjwt.review;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDTO {
    private Long recipeId;
    private String content;
    private int rating;
    private String mediaUrl;
}