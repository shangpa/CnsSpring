package com.example.springjwt.review;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRecipe_RecipeId(Long recipeId); // 특정 레시피의 리뷰 조회
}