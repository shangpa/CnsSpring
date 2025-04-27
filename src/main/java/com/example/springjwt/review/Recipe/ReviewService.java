package com.example.springjwt.review.Recipe;


import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.recipe.Recipe;
import com.example.springjwt.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    // 리뷰 작성
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto) {
        // 현재 로그인한 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        // 유저 엔티티 찾기
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 레시피 엔티티 찾기
        Recipe recipe = recipeRepository.findById(dto.getRecipeId())
                .orElseThrow(() -> new IllegalArgumentException("해당 레시피가 없습니다."));

        Review review = dto.toEntity(user, recipe);

        reviewRepository.save(review);
        return ReviewResponseDTO.fromEntity(review);
    }

    // 특정 레시피의 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByRecipe(Long recipeId) {
        List<Review> reviews = reviewRepository.findByRecipe_RecipeId(recipeId);
        return reviews.stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}