package com.example.springjwt.Mypage;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.point.PointActionType;
import com.example.springjwt.point.PointService;
import com.example.springjwt.recipe.Recipe;
import com.example.springjwt.recipe.RecipeDTO;
import com.example.springjwt.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class LikeRecipeController {

    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final UserRepository userRepository;
    private final PointService pointService;

    @PostMapping("/{recipeId}/like-toggle")
    public ResponseEntity<String> toggleLikeRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        UserEntity user = userRepository.findByUsername(username);
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피 없음"));

        // 이미 좋아요 누른 경우 → 취소
        Optional<LikeRecipe> existing = likeRecipeRepository.findByUserAndRecipe(user, recipe);
        if (existing.isPresent()) {
            likeRecipeRepository.delete(existing.get());
            return ResponseEntity.ok("좋아요 취소됨");
        }

        // 좋아요 누르기
        LikeRecipe like = new LikeRecipe();
        like.setUser(user);
        like.setRecipe(recipe);
        like.setLikedAt(LocalDateTime.now());
        likeRecipeRepository.save(like);

        // 좋아요 수 증가
        recipe.setLikes(recipe.getLikes() + 1);

        // ✅ 포인트 적립 체크
        int newStep = recipe.getLikes() / 10;
        int prevStep = recipe.getLikePointStep();

        if (newStep > prevStep) {
            int diff = newStep - prevStep;

            // 레시피 작성자에게 포인트 지급
            pointService.addPoint(
                    recipe.getUser(),
                    PointActionType.RECIPE_LIKE_RECEIVED,
                    diff * 10,
                    "레시피 좋아요 누적 " + recipe.getLikes() + "개 돌파"
            );

            recipe.setLikePointStep(newStep);
        }

        recipeRepository.save(recipe);

        return ResponseEntity.ok("좋아요 추가됨");
    }
    @GetMapping("/likes")
    public ResponseEntity<List<RecipeDTO>> getLikedRecipes(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        UserEntity user = userRepository.findByUsername(username);

        List<Recipe> likedRecipes = likeRecipeRepository.findByUser(user)
                .stream()
                .map(LikeRecipe::getRecipe)
                .collect(Collectors.toList());

        List<RecipeDTO> result = likedRecipes.stream()
                .map(RecipeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
