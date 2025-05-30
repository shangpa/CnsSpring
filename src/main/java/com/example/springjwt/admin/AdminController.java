package com.example.springjwt.admin;

import com.example.springjwt.User.JoinService;
import com.example.springjwt.admin.dto.BoardMonthlyStatsDTO;
import com.example.springjwt.admin.dto.RecipeMonthlyStatsDTO;
import com.example.springjwt.board.BoardDetailResponseDTO;
import com.example.springjwt.board.BoardRepository;
import com.example.springjwt.board.BoardService;
import com.example.springjwt.dto.JoinDTO;
import com.example.springjwt.recipe.RecipeSearchResponseDTO;
import com.example.springjwt.recipe.RecipeService;
import com.example.springjwt.report.ReportRepository;
import com.example.springjwt.report.ReportService;
import com.example.springjwt.tradepost.TradePostRepository;
import com.example.springjwt.tradepost.TradePostService;
import com.example.springjwt.tradepost.TradePostSimpleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JoinService joinService;
    private final RecipeService recipeService;
    private final AdminRecipeService adminRecipeService;
    private final TradePostService tradePostService;
    private final BoardService boardService;
    private final ReportService reportService;

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
    // 관리자 인기 레시피 3개 조회
    @GetMapping("/recipes/top3")
    public ResponseEntity<List<RecipeSearchResponseDTO>> getTop3Recipes() {
        return ResponseEntity.ok(adminRecipeService.getTop3Recipes());
    }

    //최근 4개월 동안
    @GetMapping("/monthly-stats")
    public ResponseEntity<List<RecipeMonthlyStatsDTO>> getMonthlyStats() {
        return ResponseEntity.ok(recipeService.getRecentFourMonthsStats());
    }
    
    // 관리자 인기 거래글 3개 조회
    @GetMapping("/popular/top3")
    public ResponseEntity<List<TradePostSimpleResponseDTO>> getPopularTradePosts() {
        List<TradePostSimpleResponseDTO> topPosts = tradePostService.getTop3PopularTradePosts();
        return ResponseEntity.ok(topPosts);
    }

    // 관리자 인기 커뮤니티 3개 조회
    @GetMapping("/boards/top3")
    public ResponseEntity<List<BoardDetailResponseDTO>> getTop3Boards() {
        return ResponseEntity.ok(boardService.getTop3PopularBoardsForAdmin());
    }

    //최근 4개월 커뮤니티 게시글 통계
    @GetMapping("/board/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getBoardMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        List<BoardMonthlyStatsDTO> stats = boardService.countBoardMonthly(startDate);
        return ResponseEntity.ok(stats);
    }

    //최근 4개월 커뮤니티 댓글 통계
    @GetMapping("/comment/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getCommentMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        List<BoardMonthlyStatsDTO> stats = adminRecipeService.countCommentMonthly(startDate);
        return ResponseEntity.ok(stats);
    }

    //최근 4개월 신고 통계
    @GetMapping("/report/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getReportMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<BoardMonthlyStatsDTO> stats = reportService.countReportMonthly(startDate);
        return ResponseEntity.ok(stats);
    }

}
