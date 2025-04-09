package com.example.springjwt.review;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Builder
@Getter
public class ReviewResponseDTO {
    private Long reviewId;
    private String content;
    private int rating;
    private String mediaUrls;
    private LocalDateTime createdAt;
    private String username; // 리뷰 작성자

    public static ReviewResponseDTO fromEntity(Review review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .rating(review.getRating())
                .mediaUrls(review.getMediaUrls())
                .createdAt(review.getCreatedAt())
                .username(review.getUser().getUsername())
                .build();
    }

}