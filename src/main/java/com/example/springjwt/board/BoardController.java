package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final BoardService boardService;

    //커뮤니티 게시글 작성
    @PostMapping
    public ResponseEntity<BoardResponseDTO> createBoard(@RequestBody BoardRequestDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("userDetails: " + userDetails);
        BoardResponseDTO response = boardService.create(dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
    //메인 기준
    @GetMapping("/main")
    public ResponseEntity<BoardMainDTO> getCommunityMain(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;

        List<BoardDetailResponseDTO> popular = boardService.getPopularBoards(3, username);
        List<BoardDetailResponseDTO> free = boardService.getBoardsByTypeAndSort(BoardType.FREE, "latest", 3, username);
        List<BoardDetailResponseDTO> cook = boardService.getBoardsByTypeAndSort(BoardType.COOKING, "latest", 3, username);

        BoardMainDTO result = new BoardMainDTO();
        result.setPopularBoards(popular);
        result.setFreeBoards(free);
        result.setCookBoards(cook);

        return ResponseEntity.ok(result);
    }

    //좋아요순 기준
    @GetMapping("/popular")
    public ResponseEntity<List<BoardDetailResponseDTO>> getPopularBoards(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BoardDetailResponseDTO> response = boardService.getPopularBoards();
        return ResponseEntity.ok(response);
    }


    //좋아요 기능
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeBoard(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepository.findByUsername(userDetails.getUsername());
        Board board = boardRepository.findById(id).orElseThrow();

        if (boardLikeRepository.existsByUserAndBoard(user, board)) {
            return ResponseEntity.badRequest().body("이미 추천한 게시글입니다");
        }

        BoardLike like = new BoardLike();
        like.setUser(user);
        like.setBoard(board);
        boardLikeRepository.save(like);

        int count = (int) boardLikeRepository.countByBoard(board);
        board.setLikeCount(count);
        boardRepository.save(board);

        return ResponseEntity.ok("추천 완료");
    }

    //댓글 작성
    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody CommentRequestDTO dto,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepository.findByUsername(userDetails.getUsername());
        Board board = boardRepository.findById(id).orElseThrow();

        BoardComment comment = new BoardComment();
        comment.setUser(user);
        comment.setBoard(board);
        comment.setContent(dto.getContent());

        boardCommentRepository.save(comment);

        return ResponseEntity.ok("댓글 등록 완료");
    }

    //댓글 조회
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<BoardComment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(boardCommentRepository.findByBoardIdOrderByCreatedAtAsc(id));
    }

    //타입별 페이징
    @GetMapping("/{type}")
    public ResponseEntity<List<Board>> getBoardsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BoardType boardType = BoardType.valueOf(type.toUpperCase());
        Pageable pageable;

        switch (sort) {
            case "like":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "likeCount"));
                break;
            case "comment":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "commentCount"));
                break;
            default:
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Board> boardPage = boardRepository.findByBoardType(boardType, pageable);
        return ResponseEntity.ok(boardPage.getContent());
    }

    //특정 id 조회
    @GetMapping("/{id}/detail")
    public ResponseEntity<BoardDetailResponseDTO> getBoardDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BoardDetailResponseDTO detail = boardService.getBoardDetail(id, userDetails.getUsername());
        return ResponseEntity.ok(detail);
    }

    //댓글 개수랑 댓글 조회
    @GetMapping("/{id}/commentsWithC")
    public ResponseEntity<?> getCommentsWithC(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BoardComment> comments = boardCommentRepository.findByBoardIdOrderByCreatedAtAsc(id);

        List<CommentResponseDTO> dtoList = comments.stream()
                .map(c -> new CommentResponseDTO(
                        c.getUser().getUsername(),
                        c.getContent(),
                        c.getCreatedAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(new CommentListWithCountDTO(dtoList, dtoList.size()));
    }

}
