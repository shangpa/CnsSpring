package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByBoardType(BoardType boardType);

    //좋아요순 기준
    @Query("SELECT b FROM Board b WHERE b.boardType IN (:types) ORDER BY b.likeCount DESC")
    List<Board> findPopularBoards(@Param("types") List<BoardType> types, Pageable pageable);
    //보드 타입별로 페이지수 정렬
    Page<Board> findByBoardType(BoardType boardType, Pageable pageable);

    @Query("SELECT b FROM Board b LEFT JOIN b.comments c WHERE b.boardType = :type GROUP BY b ORDER BY COUNT(c) DESC")
    List<Board> findBoardsByCommentCount(@Param("type") BoardType type, Pageable pageable);

    List<Board> findByWriter(UserEntity user);
}
