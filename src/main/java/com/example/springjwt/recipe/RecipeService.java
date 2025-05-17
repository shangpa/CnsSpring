package com.example.springjwt.recipe;

import com.example.springjwt.Mypage.LikeRecipe;
import com.example.springjwt.Mypage.LikeRecipeRepository;
import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.point.PointActionType;
import com.example.springjwt.point.PointService;
import com.example.springjwt.review.Recipe.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  RecipeService {
    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final LikeRecipeRepository likeRecipeRepository;

    // 전체 레시피 조회
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    // 공개된 레시피만 정렬해서 가져오기
    public List<RecipeSearchResponseDTO> getAllPublicRecipes(String sort) {
        List<Recipe> recipes;
        // 로그인한 사용자 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByUsername(username);

        switch (sort != null ? sort : "viewCount") {
            case "likes":
                recipes = recipeRepository.findByIsPublicTrueOrderByLikesDesc();
                break;
            case "latest":
                recipes = recipeRepository.findByIsPublicTrueOrderByCreatedAtDesc();
                break;
            case "shortTime":
                recipes = recipeRepository.findByIsPublicTrueOrderByCookingTimeAsc();
                break;
            case "longTime":
                recipes = recipeRepository.findByIsPublicTrueOrderByCookingTimeDesc();
                break;
            case "viewCount":
            default:
                recipes = recipeRepository.findByIsPublicTrueOrderByViewCountDesc();
                break;
        }

        return recipes.stream().map(recipe -> {
            Double avgRatingWrapper = reviewRepository.findAvgRatingByRecipe(recipe.getRecipeId());
            double avgRating = avgRatingWrapper != null ? avgRatingWrapper : 0.0;
            int reviewCount = reviewRepository.countByRecipe(recipe);
            boolean liked = likeRecipeRepository.existsByUserAndRecipe(currentUser, recipe);

            return RecipeSearchResponseDTO.fromEntity(recipe, avgRating, reviewCount, liked);
        }).collect(Collectors.toList());
    }

    // 특정 레시피 조회
    public Recipe getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다: " + id));
        recipe.setViewCount(recipe.getViewCount() + 1); // 조회수 1 증가
        recipeRepository.save(recipe); // 저장
        return recipe;
    }

    // 레시피 생성
    public Recipe createRecipe(RecipeDTO recipeDTO, String username) {
        UserEntity user = userRepository.findByUsername(username);
        Recipe recipe = recipeDTO.toEntity();
        System.out.println("로그인 된 유저 :"+user.getUsername());
        recipe.setUser(user); // 로그인한 유저를 레시피 작성자로 설정
        pointService.addPoint(
                user,
                PointActionType.RECIPE_WRITE,
                1,
                "레시피 작성 포인트 10점 적립"
        );
        return recipeRepository.save(recipe);
    }

    // 레시피 수정
    public Recipe updateRecipe(Long id, RecipeDTO recipeDTO) {
        Recipe existingRecipe = getRecipeById(id);
        existingRecipe.setTitle(recipeDTO.getTitle());
        existingRecipe.setCategory(RecipeCategory.valueOf(recipeDTO.getCategory()));
        existingRecipe.setIngredients(recipeDTO.getIngredients());
        existingRecipe.setAlternativeIngredients(recipeDTO.getAlternativeIngredients());
        existingRecipe.setCookingSteps(recipeDTO.getCookingSteps());
        existingRecipe.setMainImageUrl(recipeDTO.getMainImageUrl());
        existingRecipe.setDifficulty(RecipeDifficulty.valueOf(recipeDTO.getDifficulty()));
        existingRecipe.setCookingTime(recipeDTO.getCookingTime());
        existingRecipe.setServings(recipeDTO.getServings());
        existingRecipe.setPublic(recipeDTO.getIsPublic());
        return recipeRepository.save(existingRecipe);
    }

    // 레시피 삭제
    public void deleteRecipe(Long id) {
        Recipe recipe = getRecipeById(id);
        recipeRepository.delete(recipe);
    }

    // 레시피 검색
    public List<RecipeSearchResponseDTO> searchRecipesByTitle(String title) {
        List<Recipe> recipes;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByUsername(username);
        if (title == null || title.trim().isEmpty()) {
            recipes = recipeRepository.findAll();
        } else {
            recipes = recipeRepository.findByTitleContainingIgnoreCase(title);
        }

        return recipes.stream()
                .map(recipe -> {
                    Double avgRatingWrapper = reviewRepository.findAvgRatingByRecipe(recipe.getRecipeId());
                    double avgRating = avgRatingWrapper != null ? avgRatingWrapper : 0.0;
                    int reviewCount = reviewRepository.countByRecipe(recipe);
                    boolean liked = likeRecipeRepository.existsByUserAndRecipe(currentUser, recipe);

                    return RecipeSearchResponseDTO.fromEntity(recipe, avgRating, reviewCount, liked);
                })
                .collect(Collectors.toList());
    }
}