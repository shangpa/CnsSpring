package com.example.springjwt.review;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDTO {
    private Long reviewId;
    private String content;
    private int rating;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private String username; // 리뷰 작성자

    public ReviewResponseDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.mediaUrl = review.getMediaUrl();
        this.createdAt = review.getCreatedAt();
        this.username = review.getUser().getUsername(); // 사용자 이름 반환
    }
}