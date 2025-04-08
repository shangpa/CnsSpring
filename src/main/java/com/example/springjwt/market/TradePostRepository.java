package com.example.springjwt.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradePostRepository extends JpaRepository<TradePost, Long> {
}