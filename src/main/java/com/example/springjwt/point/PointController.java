package com.example.springjwt.point;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/point")
public class PointController {

    private final PointService pointService;
    private final UserRepository userRepository;

    // ✅ 1. 포인트 적립 (테스트용)
    @PostMapping("/add")
    public ResponseEntity<String> addPoint(
            @RequestParam int userId,
            @RequestParam PointActionType action,
            @RequestParam(defaultValue = "1") int count,
            @RequestParam String description
    ) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        pointService.addPoint(user, action, count, description);
        return ResponseEntity.ok("포인트가 적립되었습니다.");
    }

    // ✅ 2. 포인트 차감 (테스트용)
    @PostMapping("/use")
    public ResponseEntity<String> usePoint(
            @RequestParam int userId,
            @RequestParam int amount,
            @RequestParam String description
    ) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        pointService.usePoint(user, amount, description);
        return ResponseEntity.ok("포인트가 차감되었습니다.");
    }

    // ✅ 3. 유저 포인트 이력 조회
    @GetMapping("/user/{id}/history")
    public ResponseEntity<List<PointHistory>> getPointHistory(@PathVariable int id) {
        List<PointHistory> history = pointService.getHistory(id);
        return ResponseEntity.ok(history);
    }
}
