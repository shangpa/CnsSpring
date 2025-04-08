package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "trade_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradePost {

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

    private String location; // 거래 희망 장소 (추후 지도 기능 연동)
}