package com.example.springjwt.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findByBoardIdOrderByCreatedAtAsc(Long boardId);

    @Query("""
    SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m'), COUNT(c)
    FROM BoardComment c
    WHERE c.createdAt >= :startDate
    GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m')
""")
    List<Object[]> countCommentMonthly(@Param("startDate") LocalDateTime startDate);

    void deleteAllByBoard(Board board);
}
