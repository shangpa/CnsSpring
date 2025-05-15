package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                false
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
                liked
        );
    }
}