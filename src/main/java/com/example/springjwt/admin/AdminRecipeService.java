package com.example.springjwt.admin;

import com.example.springjwt.board.BoardRepository;
import com.example.springjwt.recipe.Recipe;
import com.example.springjwt.recipe.RecipeRepository;
import com.example.springjwt.recipe.RecipeSearchResponseDTO;
import com.example.springjwt.tradepost.TradePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRecipeService {

    private final RecipeRepository recipeRepository;
    private final TradePostRepository tradePostRepository;
    private final BoardRepository boardRepository;

    // 인기 레시피 상위 3개 조회 (관리자용)
    public List<RecipeSearchResponseDTO> getTop3Recipes() {
        List<Recipe> top3 = recipeRepository.findTop3ByIsPublicTrueOrderByViewCountDesc();

        return top3.stream()
                .map(recipe -> RecipeSearchResponseDTO.fromEntity(recipe, 0.0, 0, false))
                .collect(Collectors.toList());
    }


}
