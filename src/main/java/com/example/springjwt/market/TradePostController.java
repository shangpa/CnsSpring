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

    @PostMapping
    public ResponseEntity<TradePostDTO> createTradePost(@RequestBody TradePostDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        TradePost post = tradePostService.create(dto, userDetails.getUsername());
        return ResponseEntity.ok(TradePostDTO.fromEntity(post));
    }
}
