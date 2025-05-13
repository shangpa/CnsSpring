package com.example.springjwt.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByBoardType(BoardType boardType);

    List<Board> findTop10ByOrderByViewCountDesc(); // 인기글 기준
    List<Board> findTop10ByOrderByLikeCountDesc();
}
