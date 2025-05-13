package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody BoardRequestDTO dto, Principal principal) {
        UserEntity user = userRepository.findByUsername(principal.getName());

        Board board = new Board();
        board.setWriter(user);
        board.setContent(dto.getContent());
        board.setImageUrls(dto.getImageUrls());
        board.setBoardType(dto.getBoardType());

        return ResponseEntity.ok(boardRepository.save(board));
    }

    @GetMapping("/popular")
    public List<Board> getPopularBoards() {
        // 조회수 + 저장수 상위 (정렬 기준 커스터마이징 가능)
        return boardRepository.findTop10ByOrderByViewCountDesc();
    }

    @GetMapping("/{type}")
    public List<Board> getBoardsByType(@PathVariable String type) {
        return boardRepository.findByBoardType(BoardType.valueOf(type.toUpperCase()));
    }
}
