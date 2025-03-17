package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {
    // UserEntity의 id 필드를 기준으로 Fridge 엔티티 조회
    List<Fridge> findByUser_Id(Long userId);
}
