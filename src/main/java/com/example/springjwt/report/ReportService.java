package com.example.springjwt.report;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.board.Board;
import com.example.springjwt.board.BoardComment;
import com.example.springjwt.board.BoardCommentRepository;
import com.example.springjwt.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final UserRepository userRepository;

    public void report(ReportRequestDTO dto, String username) {
        UserEntity reporter = userRepository.findByUsername(username);
        Report report = new Report();
        report.setReporter(reporter);

        if (dto.getBoardId() != null) {
            Board board = boardRepository.findById(dto.getBoardId()).orElseThrow();
            report.setBoard(board);
        }
        if (dto.getBoardCommentId() != null) {
            BoardComment comment = boardCommentRepository.findById(dto.getBoardCommentId()).orElseThrow();
            report.setBoardComment(comment);
        }
        reportRepository.save(report);
    }
}