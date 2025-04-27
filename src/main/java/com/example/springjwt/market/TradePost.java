package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "trade_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradePost {

    public static final int STATUS_ONGOING = 0; //거래중
    public static final int STATUS_COMPLETED = 1; //거래완료
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradePostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;  // 작성자 (외래 키)

    private String category;

    private String title;

    private int quantity;

    private int price;

    private LocalDate purchaseDate;

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String imageUrls; // JSON 형식의 이미지 URL 리스트

    private String location; // 거래 희망 장소 (추후 지도 기능 연동)
    @Column(nullable = false)
    private int status = STATUS_ONGOING;  // 기본값 0, 거래중

    public String extractFirstImageUrl() {
        if (this.imageUrls == null || this.imageUrls.isEmpty()) {
            return null; // 이미지가 없으면 null 반환
        }
        try {
            // 이미지 URL들을 JSON 문자열로 저장했으니까 파싱해줘야 함
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> urls = objectMapper.readValue(this.imageUrls, new TypeReference<List<String>>() {});

            if (urls.isEmpty()) {
                return null;
            }
            return urls.get(0); // 첫 번째 이미지 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}