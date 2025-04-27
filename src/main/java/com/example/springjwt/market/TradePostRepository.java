package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradePostRepository extends JpaRepository<TradePost, Long> {
    List<TradePost> findByUser(UserEntity user);
    //카테고리
    List<TradePost> findByCategory(String category);
    //검색
    List<TradePost> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleKeyword, String descriptionKeyword);
}
