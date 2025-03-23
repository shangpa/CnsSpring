package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {
    //조회기준
    List<Fridge> findByUserIdOrderByUpdatedAtDesc(Long userId);

}
