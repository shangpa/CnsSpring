package com.example.springjwt.review.Recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    // 특정 레시피의 리뷰 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable Long recipeId) {
        return ResponseEntity.ok(reviewService.getReviewsByRecipe(recipeId));
    }
}