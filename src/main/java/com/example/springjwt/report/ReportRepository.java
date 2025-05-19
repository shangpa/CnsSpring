package com.example.springjwt.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // Optional<Report> findByReporterAndBoard(UserEntity reporter, Board board);
    // Optional<Report> findByReporterAndBoardComment(UserEntity reporter, BoardComment boardComment);
}