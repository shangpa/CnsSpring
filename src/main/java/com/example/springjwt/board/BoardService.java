package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;

    // 작성
    public BoardResponseDTO create(BoardRequestDTO dto, String username) {
        UserEntity user = userRepository.findByUsername(username);
        Board board = new Board();
        board.setWriter(user);
        board.setContent(dto.getContent());
        board.setImageUrls(dto.getImageUrls());
        board.setBoardType(dto.getBoardType());

        Board saved = boardRepository.save(board);

        return new BoardResponseDTO(
                saved.getId(), saved.getContent(), user.getUsername(),
                saved.getImageUrls(), saved.getBoardType().toString(), saved.getCreatedAt()
        );
    }
    // 인기 Top 10
    public List<BoardDetailResponseDTO> getPopularBoards() {
        List<BoardType> types = List.of(BoardType.COOKING, BoardType.FREE);
        Pageable pageable = PageRequest.of(0, 10);

        List<Board> boards = boardRepository.findPopularBoards(types, pageable);

        return boards.stream().map(board -> new BoardDetailResponseDTO(
                board.getId(),
                board.getContent(),
                board.getWriter().getUsername(),
                board.getImageUrls(),
                board.getBoardType().name(),
                board.getCreatedAt().toString(),
                (int) boardLikeRepository.countByBoard(board),
                false,
                board.getCommentCount()
        )).toList();
    }

    //특정 id 조회
    public BoardDetailResponseDTO getBoardDetail(Long id, String username) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        UserEntity user = userRepository.findByUsername(username);
        boolean liked = boardLikeRepository.existsByUserAndBoard(user, board);

        return new BoardDetailResponseDTO(
                board.getId(),
                board.getContent(),
                board.getWriter().getUsername(),
                board.getImageUrls(),
                board.getBoardType().toString(),
                board.getCreatedAt().toString(),
                board.getLikeCount(),
                liked,
                board.getCommentCount()
        );
    }

    // 인기게시판: 타입 상관없이 좋아요순 TOP N
    public List<BoardDetailResponseDTO> getPopularBoards(int limit, String username) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "likeCount"));
        List<Board> boards = boardRepository.findAll(pageable).getContent();

        return boards.stream()
                .map(board -> toDetailDTO(board, username))
                .toList();
    }
    // 타입별 최신순 TOP N
    public List<BoardDetailResponseDTO> getBoardsByTypeAndSort(BoardType type, String sort, int limit, String username) {
        Sort sorting = sort.equals("like") ?
                Sort.by(Sort.Direction.DESC, "likeCount") :
                Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, limit, sorting);
        List<Board> boards = boardRepository.findByBoardType(type, pageable).getContent();

        return boards.stream()
                .map(board -> toDetailDTO(board, username))
                .toList();
    }

    // 변환 함수
    private BoardDetailResponseDTO toDetailDTO(Board board, String username) {
        boolean liked = false;
        if (username != null) {
            liked = boardLikeRepository.existsByUserUsernameAndBoard(username, board);
        }
        return new BoardDetailResponseDTO(
                board.getId(),
                board.getContent(),
                board.getWriter().getUsername(),
                board.getImageUrls(),
                board.getBoardType().toString(),
                board.getCreatedAt().toString(),
                board.getLikeCount(),
                liked,
                board.getCommentCount()
        );
    }

    public List<BoardDetailResponseDTO> getBoardsByTypePaged(BoardType type, String sort, int page, int size, String username) {
        Sort sorting = switch (sort) {
            case "like" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "comment" -> Sort.by(Sort.Direction.DESC, "commentCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = PageRequest.of(page, size, sorting);
        List<Board> boards = boardRepository.findByBoardType(type, pageable).getContent();
        return boards.stream().map(b -> toDetailDTO(b, username)).toList();
    }
}