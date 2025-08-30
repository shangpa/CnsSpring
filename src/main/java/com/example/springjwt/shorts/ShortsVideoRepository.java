package com.example.springjwt.shorts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShortsVideoRepository extends JpaRepository<ShortsVideo, Long> {
    List<ShortsVideo> findTop10ByIsPublicTrueOrderByCreatedAtDesc(); // 최신순
    List<ShortsVideo> findTop10ByIsPublicTrueOrderByViewCountDesc(); // 인기순
}
