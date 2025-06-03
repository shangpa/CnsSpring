package com.example.springjwt.User;

import com.example.springjwt.admin.dto.UserListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

    Optional<UserEntity> findOptionalByUsername(String username);

    Page<UserListDTO> findAllBy(Pageable pageable);

    // 차단된 유저만 페이징 조회
    Page<UserEntity> findByBlockedTrue(Pageable pageable);
}
