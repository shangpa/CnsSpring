package com.example.springjwt.board;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    boolean existsByUserAndBoard(UserEntity user, Board board);
    Optional<BoardLike> findByUserAndBoard(UserEntity user, Board board);
}
