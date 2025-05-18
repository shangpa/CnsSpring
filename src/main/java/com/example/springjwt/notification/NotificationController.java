package com.example.springjwt.notification;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @RequestHeader("Authorization") String token) {

        String username = jwtUtil.getUsername(token);
        UserEntity user = userRepository.findByUsername(username);

        List<NotificationEntity> notifications = notificationRepository
                .findByUserOrderByCreatedAtDesc(user);

        // DTO 변환
        List<NotificationResponseDTO> dtoList = notifications.stream()
                .map(NotificationResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }
}
