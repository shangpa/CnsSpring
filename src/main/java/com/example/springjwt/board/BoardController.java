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
    private final BoardLikeRepository boardLikeRepository;
    private final BoardCommentRepository boardCommentRepository;

    //커뮤니티 게시글 작성
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

    //조회수랑 추천수 기준 정렬
    @GetMapping("/popular")
    public List<Board> getPopularBoards() {
        // 조회수 + 저장수 상위 (정렬 기준 커스터마이징 가능)
        return boardRepository.findTop10ByOrderByViewCountDesc();
    }

    //타입별 정렬
    @GetMapping("/{type}")
    public List<Board> getBoardsByType(@PathVariable String type) {
        return boardRepository.findByBoardType(BoardType.valueOf(type.toUpperCase()));
    }

    //좋아요 기능
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeBoard(@PathVariable Long id, Principal principal) {
        UserEntity user = userRepository.findByUsername(principal.getName());
        Board board = boardRepository.findById(id).orElseThrow();

        if (boardLikeRepository.existsByUserAndBoard(user, board)) {
            return ResponseEntity.badRequest().body("이미 추천한 게시글입니다");
        }

        BoardLike like = new BoardLike();
        like.setUser(user);
        like.setBoard(board);
        boardLikeRepository.save(like);

        board.setLikeCount(board.getLikeCount() + 1);
        boardRepository.save(board);

        return ResponseEntity.ok("추천 완료");
    }

    //댓글 작성
    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody CommentRequestDTO dto,
                                        Principal principal) {
        UserEntity user = userRepository.findByUsername(principal.getName());
        Board board = boardRepository.findById(id).orElseThrow();

        BoardComment comment = new BoardComment();
        comment.setUser(user);
        comment.setBoard(board);
        comment.setContent(dto.getContent());

        boardCommentRepository.save(comment);

        return ResponseEntity.ok("댓글 등록 완료");
    }

    //댓글 작성
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<BoardComment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(boardCommentRepository.findByBoardIdOrderByCreatedAtAsc(id));
    }

}
