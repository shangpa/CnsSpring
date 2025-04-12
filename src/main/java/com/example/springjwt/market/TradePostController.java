package com.example.springjwt.market;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trade-posts")
@RequiredArgsConstructor
public class TradePostController {
    private final TradePostService tradePostService;

    // 거래글 생성
    @PostMapping
    public ResponseEntity<TradePostDTO> createTradePost(@RequestBody TradePostDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        TradePost post = tradePostService.create(dto, userDetails.getUsername());
        return ResponseEntity.ok(TradePostDTO.fromEntity(post));
    }

    // 거래글 상세 조회 (ID로)
    @GetMapping("/{id}")
    public ResponseEntity<TradePostDTO> getTradePostById(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        TradePostDTO tradePostDTO = tradePostService.getTradePostById(id);
        return ResponseEntity.ok(tradePostDTO);
    }
}
