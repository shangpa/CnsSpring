package com.example.springjwt.review.TradePost;

import com.example.springjwt.tradepost.TradePost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TpReviewRepository extends JpaRepository<TpReview, Long> {
    List<TpReview> findByTradePost_TradePostId(Long tradePostId); // 특정 거래글 리뷰 조회

    List<TpReview> findByUser_Id(int userId);
    List<TpReview> findByTradePost_User_Id(int userId);
    void deleteAllByTradePost(TradePost post);
}