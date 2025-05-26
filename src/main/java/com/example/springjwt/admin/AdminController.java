package com.example.springjwt.admin;

import com.example.springjwt.User.JoinService;
import com.example.springjwt.admin.dto.RecipeMonthlyStatsDTO;
import com.example.springjwt.dto.JoinDTO;
import com.example.springjwt.recipe.RecipeSearchResponseDTO;
import com.example.springjwt.recipe.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JoinService joinService;
    private final RecipeService recipeService;
    private final AdminRecipeService adminRecipeService;


    // 관리자 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> adminJoin(@RequestBody JoinDTO joinDTO) {
        boolean success = joinService.joinAdminProcess(joinDTO);
        if (success) {
            return ResponseEntity.ok("✅ 관리자 회원가입 성공");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 관리자 아이디입니다");
        }
    }

    // 테스트용 관리자 전용 API
    @GetMapping("/test")
    public ResponseEntity<String> adminOnlyApi() {
        System.out.println("관리자 :"+Thread.currentThread().getName()+"로그인");
        return ResponseEntity.ok("✅ 관리자 전용 API 접근 성공");
    }
    // ✅ 관리자 인기 레시피 3개 조회
    @GetMapping("/recipes/top3")
    public ResponseEntity<List<RecipeSearchResponseDTO>> getTop3Recipes() {
        return ResponseEntity.ok(adminRecipeService.getTop3Recipes());
    }

    //최근 4개월 동안
    @GetMapping("/monthly-stats")
    public ResponseEntity<List<RecipeMonthlyStatsDTO>> getMonthlyStats() {
        return ResponseEntity.ok(recipeService.getRecentFourMonthsStats());
    }
}
