package com.example.springjwt.recipe;

import com.example.springjwt.search.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final SearchKeywordService searchKeywordService; // 검색 기록 저장용 서비스 추가

    // 레시피 전체 조회
    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        List<RecipeDTO> recipes = recipeService.getAllRecipes()
                .stream()
                .map(RecipeDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recipes);
    }
    
    // 공개 레시피 조회
    @GetMapping("/public")
    public List<RecipeSearchResponseDTO> getPublicRecipes(@RequestParam(required = false) String sort) {
        return recipeService.getAllPublicRecipes(sort);
    }

    // 특정 레시피 조회
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(RecipeDTO.fromEntity(recipe));
    }

    // 레시피 생성
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @RequestBody RecipeDTO recipeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("Received RecipeDTO: " + recipeDTO);
        Recipe recipe = recipeService.createRecipe(recipeDTO, userDetails.getUsername());

        RecipeResponseDTO response = new RecipeResponseDTO(
                true,
                "레시피가 성공적으로 생성되었습니다.",
                recipe.getRecipeId()
        );
        return ResponseEntity.ok(response);
    }

    // 레시피 수정
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> updateRecipe(@PathVariable Long id, @RequestBody RecipeDTO recipeDTO) {
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDTO);
        return ResponseEntity.ok(RecipeDTO.fromEntity(updatedRecipe));
    }

    // 레시피 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // 레시피 검색 + 검색어 저장
    @GetMapping("/search")
    public ResponseEntity<List<RecipeSearchResponseDTO>> searchRecipes(
            @RequestParam(required = false) String title) {

        System.out.println("search title: " + title);

        if (title != null && !title.isBlank()) {
            searchKeywordService.saveKeyword(title); // 검색어 저장
        }

        List<RecipeSearchResponseDTO> recipes = recipeService.searchRecipesByTitle(title);
        return ResponseEntity.ok(recipes);
    }

}
