package com.example.springjwt.admin;

import com.example.springjwt.User.JoinService;
import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.User.UserService;
import com.example.springjwt.admin.dto.*;
import com.example.springjwt.admin.log.AdminLog;
import com.example.springjwt.admin.log.AdminLogRepository;
import com.example.springjwt.admin.log.AdminLogService;
import com.example.springjwt.board.BoardDetailResponseDTO;
import com.example.springjwt.board.BoardRepository;
import com.example.springjwt.board.BoardService;
import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.dto.JoinDTO;
import com.example.springjwt.admin.dto.PointHistoryDTO;
import com.example.springjwt.point.PointHistoryRepository;
import com.example.springjwt.point.PointService;
import com.example.springjwt.recipe.RecipeRepository;
import com.example.springjwt.recipe.RecipeSearchResponseDTO;
import com.example.springjwt.recipe.RecipeService;
import com.example.springjwt.report.ReportRepository;
import com.example.springjwt.report.ReportService;
import com.example.springjwt.review.Recipe.ReviewRepository;
import com.example.springjwt.review.TradePost.TpReviewRepository;
import com.example.springjwt.tradepost.TradePostRepository;
import com.example.springjwt.tradepost.TradePostService;
import com.example.springjwt.tradepost.TradePostSimpleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final TradePostRepository tradePostRepository;
    private final ReviewRepository reviewRepository;
    private final PointService pointService;
    private final AdminService adminService;
    private final TpReviewRepository tpReviewRepository;
    private final AdminLogRepository adminLogRepository;

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

    //최근 4개월 레시피 통계
    @GetMapping("/recipe/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getRecipeMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        return ResponseEntity.ok(recipeService.countRecipeMonthly(startDate));
    }

    //최근 4개월 레시피 조회수 통계
    @GetMapping("/recipe/views/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getRecipeViewsMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        return ResponseEntity.ok(recipeService.sumRecipeViewsMonthly(startDate));
    }

    // 최근 4개월 전체 거래글 통계
    @GetMapping("/trade/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getTradePostMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        return ResponseEntity.ok(tradePostService.countTradePostMonthly(startDate));
    }

    // 최근 4개월 무료 거래글 통계
    @GetMapping("/trade/free/monthly")
    public ResponseEntity<List<BoardMonthlyStatsDTO>> getFreeTradePostMonthlyStats() {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(3)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        return ResponseEntity.ok(tradePostService.countFreeTradePostMonthly(startDate));
    }

    /**
     * 관리자용 회원 리스트 조회 (페이징)
     * - 응답: 회원 id, 이름(name), 아이디(username)
     * - GET /api/admin/users?page=0&size=10
     */
    @GetMapping("/users")
    public Page<UserListDTO> getUserList(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return userRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        ).map(user -> new UserListDTO(
                user.getId(),
                user.getName(),
                user.getUsername()
        ));
    }

    /**
     * 관리자용 회원 상세 정보 조회
     * - 응답: 이름, 아이디, 가입일, 포인트, 작성한 레시피 수, 거래글 수, 리뷰 수
     * - GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public UserDetailDTO getUserDetail(@PathVariable int userId){
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        int recipeCount = recipeRepository.countByUser(user);
        int tradePostCount = tradePostRepository.countByUser(user);
        int reviewCount = reviewRepository.countByUser(user);

        return new UserDetailDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getPoint(),
                recipeCount,
                tradePostCount,
                reviewCount
        );
    }

    /**
     * 특정 회원이 작성한 레시피 리스트 조회 (페이징)
     * - 응답: username, 레시피 제목, 작성일
     * - GET /api/admin/users/{userId}/recipes?page=0&size=10
     */
    @GetMapping("/users/{userId}/recipes")
    public Page<UserRecipeSimpleDTO> getUserRecipes(
            @PathVariable int userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return recipeRepository.findRecipesByUserId(userId, pageable);
    }

    /**
     * 특정 회원이 작성한 레시피 중 제목으로 검색 (페이징)
     * - GET /api/admin/users/{userId}/recipes/search?keyword=된장&page=0&size=10
     * - 응답: username, 레시피 제목, 작성일
     */
    @GetMapping("/users/{userId}/recipes/search")
    public Page<UserRecipeSimpleDTO> searchUserRecipesByTitle(
            @PathVariable int userId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return recipeRepository.findRecipesByUserIdAndTitleContains(userId, keyword, pageable);
    }

    /**
     * [GET] /api/admin/tradeposts?page=0&size=10&status=0&sortBy=createdAt
     * 전체 거래글 조회 (status: 0=거래중, 1=거래완료, 생략 시 전체)
     * 정렬 기준: createdAt, category 등 (기본값: createdAt 내림차순)
     * 응답: id, username, title, createdAt, category, status 포함
     */
    @GetMapping
    public Page<TradePostListResponseDTO> getTradePostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortBy
    ) {
        return tradePostService.getTradePosts(page, size, status, sortBy);
    }


    /**
     * [GET] /api/admin/tradeposts/{postId}
     * 거래글 상세 조회
     * 응답: id, username, title, description, createdAt, imageUrls, location, chatCount, viewCount 포함
     */
    @GetMapping("/tradeposts/{postId}")
    public ResponseEntity<TradePostDetailResponseDTO> getTradePostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(tradePostService.getTradePostDetail(postId));
    }

    /** todo 수정해야함
     * [관리자용 거래글 삭제 API]
     *
     * 거래글을 삭제하면서 삭제한 관리자 ID와 사유를 함께 전달받아 로그로 기록합니다.
     *
     * 요청 방식: DELETE
     * 요청 URL: /api/admin/tradeposts/{postId}
     * 요청 바디:
     * {
     *   "adminUsername": "admin01",
     *   "reason": "허위 게시글로 판단되어 삭제"
     * }
     * 응답: "삭제 및 로그 기록 완료"
     */
    @DeleteMapping("/tradeposts/{postId}")
    public ResponseEntity<String> deleteTradePostAsAdmin(
            @PathVariable Long postId,
            @RequestBody DeleteRequestDTO requestDTO
    ) {
        tradePostService.deletePostByAdmin(postId, requestDTO.getAdminUsername(), requestDTO.getReason());
        return ResponseEntity.ok("삭제 및 로그 기록 완료");
    }

    // [GET] /api/admin/boards
    // 관리자용 커뮤니티 게시글 조회 (페이징+정렬)
    // - page: 페이지 번호 (기본 0)
    // - size: 페이지 크기 (기본 10)
    // - sortBy: 정렬 기준 (기본 createdAt)
    // 응답: id, 작성자, 내용, 게시날짜 포함
    @GetMapping("boards")
    public Page<BoardAdminListResponseDTO> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return boardService.getBoards(pageRequest);
    }


    /* [GET] /api/admin/boards/{boardId}
     관리자용 게시글 상세 조회 API
     - boardId: 게시글 ID (PathVariable)
     - 댓글 리스트 포함
     - 응답: id, 작성자, 내용, 이미지, 날짜, 좋아요수, 댓글수, 댓글들
    */
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<BoardDetailAdminDTO> getBoardDetail(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardService.getBoardDetail(boardId));
    }

    /**
     * [DELETE] /api/admin/boards/{boardId}
     * 관리자 커뮤니티 게시글 삭제
     * - 요청: 관리자 아이디(adminUsername), 삭제 사유(reason)
     * - 응답: "삭제 및 로그 기록 완료"
     */
    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<String> deleteBoardAsAdmin(
            @PathVariable Long boardId,
            @RequestBody DeleteRequestDTO requestDTO
    ) {
        boardService.deleteBoardByAdmin(boardId, requestDTO.getAdminUsername(), requestDTO.getReason());
        return ResponseEntity.ok("삭제 및 로그 기록 완료");
    }

    // [DELETE] /api/admin/comments/{commentId}
    // 관리자 댓글 삭제 (사유 기록 및 댓글수 감소 포함)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteCommentAsAdmin(
            @PathVariable Long commentId,
            @RequestBody DeleteRequestDTO requestDTO
    ) {
        boardService.deleteCommentByAdmin(commentId, requestDTO.getAdminUsername(), requestDTO.getReason());
        return ResponseEntity.ok("댓글 삭제 및 로그 기록 완료");
    }

    /**
     * 관리자용 레시피 리스트 조회 API
     * - 페이지 번호(page)와 페이지 크기(size)를 기준으로 레시피 리스트를 페이징 처리하여 반환
     * - 반환 필드: recipeId, username(작성자 아이디), title, createdAt(작성일시)
     *
     * @param page 조회할 페이지 번호 (기본값: 0)
     * @param size 한 페이지당 레시피 개수 (기본값: 10)
     * @return Page<RecipeListAdminDTO> 형태로 페이징된 레시피 정보 반환
     */
    @GetMapping("/recipes")
    public ResponseEntity<Page<RecipeListAdminDTO>> getAllRecipesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminRecipeService.getRecipeListForAdmin(page, size));
    }

    /**
     * 관리자용 레시피 제목 검색 API
     * - 제목에 특정 키워드가 포함된 레시피를 검색
     *
     * @param title 검색할 제목 키워드 (필수)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 제목에 키워드가 포함된 레시피 목록 (페이징)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RecipeListAdminDTO>> searchRecipesByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminRecipeService.searchRecipesByTitle(title, page, size));
    }
    /**
     * 특정 회원이 작성한 판매 거래글 리스트 조회
     * - 요청 경로: /api/admin/users/{userId}/sales
     * - 응답: 거래글 ID, 제목, 작성일, 거래 상태 (0=거래중, 1=거래완료)
     */
    @GetMapping("/users/{userId}/sales")
    public List<UserTradePostSimpleDTO> getUserSales(@PathVariable int userId) {
        return tradePostRepository.findSalesByUserId(userId);
    }
    /**
     * 특정 회원이 구매한 거래글 리스트 조회
     * - 요청 경로: /api/admin/users/{userId}/purchases
     * - 응답: 거래글 ID, 제목, 작성일, 거래 상태 (0=거래중, 1=거래완료)
     */
    @GetMapping("/users/{userId}/purchases")
    public List<UserTradePostSimpleDTO> getUserPurchases(@PathVariable int userId) {
        return tradePostRepository.findPurchasesByUserId(userId);
    }

    /**
     * ✅ 특정 회원의 포인트 적립 내역 조회
     * GET /api/admin/users/{userId}/points/earned
     */
    @GetMapping("/users/{userId}/points/earned")
    public List<PointHistoryDTO> getEarnedPoints(@PathVariable int userId) {
        return pointService.getEarnedHistory(userId).stream()
                .map(PointHistoryDTO::from)
                .toList();
    }

    /**
     * ✅ 특정 회원의 포인트 사용 내역 조회
     * GET /api/admin/users/{userId}/points/used
     */
    @GetMapping("/users/{userId}/points/used")
    public List<PointHistoryDTO> getUsedPoints(@PathVariable int userId) {
        return pointService.getUsedHistory(userId).stream()
                .map(PointHistoryDTO::from)
                .toList();
    }
    /**
     * [POST] /api/admin/users/{userId}/block
     * 관리자 회원 차단 API
     * - PathVariable: userId (차단할 회원 id)
     * - RequestBody: {"reason": "스팸 계정으로 확인되어 차단"}
     */
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<?> blockUser(
            @PathVariable int userId,
            @RequestBody BlockRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails admin // 관리자의 username
    ) {
        adminService.blockUser(userId, admin.getUsername(), dto.getReason());
        return ResponseEntity.ok("차단 완료");
    }
    /**
     * [POST] /api/admin/users/{userId}/block
     * 관리자 회원 차단 해제 API
     * - PathVariable: userId (차단할 회원 id)
     * - RequestBody: {"reason": "생일기념 차단 해제"}
     */
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<?> unblockUser(
            @PathVariable int userId,
            @RequestBody BlockRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails admin
    ) {
        adminService.unblockUser(userId, admin.getUsername(), dto.getReason());
        return ResponseEntity.ok("차단 해제 완료");
    }
    /**
     * 🔍 관리자 레시피 상세 조회 (리뷰 포함)
     * @param recipeId 레시피 ID
     * @return RecipeDetailAdminDTO
     * reviews는 List로 넘어감
     */
    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecipeDetailAdminDTO> getRecipeDetail(@PathVariable Long recipeId) {
        return ResponseEntity.ok(adminRecipeService.getRecipeDetail(recipeId));
    }

    /**
     * 특정 회원이 작성한 거래 후기 조회
     * - GET /api/admin/users/{userId}/reviews/written
     */
    @GetMapping("/users/{userId}/reviews/written")
    public List<TpReviewSimpleDTO> getWrittenTradeReviews(@PathVariable int userId) {
        return tpReviewRepository.findReviewsWrittenByUser(userId);
    }

    /**
     * 특정 회원이 받은 거래 후기 조회 (내 거래글에 남겨진)
     * - GET /api/admin/users/{userId}/reviews/received
     */
    @GetMapping("/users/{userId}/reviews/received")
    public List<TpReviewSimpleDTO> getReceivedTradeReviews(@PathVariable int userId) {
        return tpReviewRepository.findReviewsReceivedByUser(userId);
    }

    /**
     * [GET] /api/admin/users/blocked
     * 차단된 회원 리스트 조회 (페이징)
     * - 응답: 회원 id, 이름(name), 아이디(username), 차단일(blockedAt)
     */
    @GetMapping("/users/blocked")
    public Page<UserBlockedListDTO> getBlockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userRepository.findByBlockedTrue(
                PageRequest.of(page, size, Sort.by("blockedAt").descending())
        ).map(user -> new UserBlockedListDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getBlockedAt()
        ));
    }

    /**
     * [GET] /api/admin/users/{userId}/block-reason
     * 차단된 회원의 최신 차단 사유/관리자/일시 조회
     * - PathVariable: userId
     * - 응답: 사유(reason), 차단관리자(blockedBy), 차단일(blockedAt)
     */
    @GetMapping("/users/{userId}/block-reason")
    public ResponseEntity<UserBlockReasonDTO> getBlockReason(@PathVariable int userId) {
        // 1. 유저 차단 여부 체크 (옵션)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        if (!user.isBlocked()) {
            return ResponseEntity.badRequest().body(null);
        }
        // 2. 최신 차단 로그 조회
        List<AdminLog> logs = adminLogRepository.findRecentUserBlocks(userId);
        if (logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AdminLog lastBlock = logs.get(0);
        UserBlockReasonDTO dto = new UserBlockReasonDTO(
                lastBlock.getReason(),
                lastBlock.getAdminUsername(),
                lastBlock.getCreatedAt()
        );
        return ResponseEntity.ok(dto);
    }
}
