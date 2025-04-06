package com.example.springjwt.search;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    @Query("SELECT k.keyword, COUNT(k.keyword) as cnt FROM SearchKeyword k GROUP BY k.keyword ORDER BY cnt DESC")
    List<Object[]> findTopKeywords(Pageable pageable);
}
