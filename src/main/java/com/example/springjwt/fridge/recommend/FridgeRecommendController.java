package com.example.springjwt.fridge.recommend;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fridge/recommend")
@RequiredArgsConstructor
public class FridgeRecommendController {

    private final FridgeRecommendService fridgeRecommendService;

    @PostMapping
    public ResponseEntity<List<RecipeRecommendResponseDTO>> recommendRecipes(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody RecipeRecommendRequestDTO requestDTO) {

        List<RecipeRecommendResponseDTO> recommendedRecipes = fridgeRecommendService.recommendRecipes(requestDTO.getSelectedIngredients());

        return ResponseEntity.ok(recommendedRecipes);
    }
}