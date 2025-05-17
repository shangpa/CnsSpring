package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserService;
import com.example.springjwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trade-posts")
@RequiredArgsConstructor
public class TradePostController {

    private final TradePostService tradePostService;
    private final UserService userService;

    // 거래글 생성
    @PostMapping
    public ResponseEntity<TradePostDTO> createTradePost(@RequestBody TradePostDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        TradePost post = tradePostService.create(dto, userDetails.getUsername());
        return ResponseEntity.ok(TradePostDTO.fromEntity(post));
    }

    // 거래글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<TradePostDTO> getTradePostById(@PathVariable Long id) {
        TradePostDTO tradePostDTO = tradePostService.getTradePostById(id);
        return ResponseEntity.ok(tradePostDTO);
    }

    // 거래 완료 처리
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TradePostDTO> completeTradePost(@PathVariable Long id) {
        TradePost completedPost = tradePostService.completeTradePost(id);
        return ResponseEntity.ok(TradePostDTO.fromEntity(completedPost));
    }

    // 내가 작성한 거래글
    @GetMapping("/my-posts")
    public ResponseEntity<List<TradePostSimpleResponseDTO>> getMyTradePosts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<TradePostSimpleResponseDTO> myPosts = tradePostService.getMyTradePosts(userDetails.getUsername());
        return ResponseEntity.ok(myPosts);
    }

    // 전체 거래글
    @GetMapping
    public ResponseEntity<List<TradePostDTO>> getAllTradePosts() {
        List<TradePostDTO> tradePosts = tradePostService.getAllTradePosts();
        return ResponseEntity.ok(tradePosts);
    }

    // 카테고리 필터
    @GetMapping("/category")
    public ResponseEntity<List<TradePostDTO>> getTradePostsByCategory(@RequestParam("category") String category) {
        List<TradePostDTO> tradePosts = tradePostService.getTradePostsByCategory(category);
        return ResponseEntity.ok(tradePosts);
    }

    // 키워드 검색
    @GetMapping("/search")
    public ResponseEntity<List<TradePostDTO>> searchTradePosts(@RequestParam("keyword") String keyword) {
        List<TradePostDTO> result = tradePostService.searchTradePosts(keyword);
        return ResponseEntity.ok(result);
    }

    // 사용자 위치 기준 1km 이내 거래글
    @GetMapping("/nearby")
    public ResponseEntity<List<TradePostDTO>> getNearbyTradePosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "1.0") double distanceKm
    ) {
        List<TradePostDTO> nearby = tradePostService.getNearbyTradePosts(userDetails.getUsername(), distanceKm);
        return ResponseEntity.ok(nearby);
    }

    // 거리순
    @GetMapping("/sorted-by-distance")
    public ResponseEntity<List<TradePostDTO>> getSortedByDistance(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        List<TradePostDTO> posts = tradePostService.getTradePostsSortedByDistance(username);
        return ResponseEntity.ok(posts);
    }

    // 카테고리 + 거리순 필터링
    @GetMapping("/nearby/filter")
    public ResponseEntity<List<TradePostDTO>> getNearbyByCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam double distanceKm,
            @RequestParam String category
    ) {
        List<TradePostDTO> result = tradePostService.getNearbyByCategory(userDetails.getUsername(), distanceKm, category);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/nearby-by-multiple-categories")
    public ResponseEntity<List<TradePostDTO>> getNearbyPostsByMultipleCategories(
            @RequestParam double distanceKm,
            @RequestParam List<String> categories,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<TradePostDTO> posts = tradePostService.getNearbyPostsByMultipleCategories(
                userDetails.getUserEntity(), distanceKm, categories
        );
        return ResponseEntity.ok(posts);
    }

}
