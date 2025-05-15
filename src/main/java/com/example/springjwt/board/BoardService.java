package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

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
}