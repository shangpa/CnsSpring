package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradePostDTO {
    private Long tradePostId;       // 거래글 ID
    private String writer;          // 작성자 username (출력용)
    private String category;        // 카테고리
    private String title;           // 제목
    private int quantity;           // 수량
    private int price;              // 거래 가격
    private LocalDate purchaseDate; // 구매 날짜
    private String description;     // 설명
    private String location;        // 거래 희망 장소 (추후 사용)
    private String imageUrls;       // 이미지 URL 목록 (JSON 문자열)
    private String createdAt;

    // DTO → Entity (User는 외부에서 주입)
    public TradePost toEntity() {
        return TradePost.builder()
                .category(category)
                .title(title)
                .quantity(quantity)
                .price(price)
                .purchaseDate(purchaseDate)
                .description(description)
                .location(location)
                .imageUrls(imageUrls)
                .build();
    }

    // Entity → DTO
    public static TradePostDTO fromEntity(TradePost tradePost) {
        return TradePostDTO.builder()
                .tradePostId(tradePost.getTradePostId())
                .writer(tradePost.getUser().getUsername())
                .category(tradePost.getCategory())
                .title(tradePost.getTitle())
                .quantity(tradePost.getQuantity())
                .price(tradePost.getPrice())
                .purchaseDate(tradePost.getPurchaseDate())
                .description(tradePost.getDescription())
                .location(tradePost.getLocation())
                .imageUrls(tradePost.getImageUrls())
                .createdAt(tradePost.getCreatedAt().toString())
                .build();
    }
}