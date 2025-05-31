package com.example.springjwt.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // Optional<Report> findByReporterAndBoard(UserEntity reporter, Board board);
    // Optional<Report> findByReporterAndBoardComment(UserEntity reporter, BoardComment boardComment);


    @Query("""
    SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m'), COUNT(r)
    FROM Report r
    WHERE r.createdAt >= :startDate
    GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
""")
    List<Object[]> countReportMonthlyRaw(@Param("startDate") LocalDateTime startDate);

}