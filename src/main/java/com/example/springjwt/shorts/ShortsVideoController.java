package com.example.springjwt.shorts;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserService;
import com.example.springjwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
public class ShortsVideoController {

    private final ShortsVideoService shortsVideoService;
    private final ShortsVideoRepository shortsVideoRepository;
    // 파일만 업로드
    @PostMapping("/upload-file")
    public ResponseEntity<String> uploadShortsFileOnly(
            @RequestParam("video") MultipartFile video,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // 파일만 저장하고 DB 등록은 안 함
            String uploadDir = "uploads/shorts/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + video.getOriginalFilename();
            File saveFile = new File(uploadDir, fileName);
            video.transferTo(saveFile);

            // 프론트에서 미리보기 할 수 있게 URL만 리턴
            String videoUrl = "/uploads/shorts/" + fileName;
            return ResponseEntity.ok(videoUrl);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("업로드 실패: " + e.getMessage());
        }
    }

    // 최종 등록
    @PostMapping("/register")
    public ResponseEntity<Long> registerShorts(
            @RequestBody RecipeShortCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            UserEntity user = userDetails.getUserEntity();
            ShortsVideo shortsVideo = ShortsVideo.builder()
                    .title(request.getTitle())
                    .videoUrl(request.getVideoUrl())
                    .isPublic(request.isPublic())
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();

            ShortsVideo saved = shortsVideoRepository.save(shortsVideo);
            return ResponseEntity.ok(saved.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 최신 숏츠
    @GetMapping("/latest")
    public ResponseEntity<List<ShortsVideo>> getLatest() {
        return ResponseEntity.ok(shortsVideoService.getLatestShorts());
    }

    // 인기 숏츠
    @GetMapping("/popular")
    public ResponseEntity<List<ShortsVideo>> getPopular() {
        return ResponseEntity.ok(shortsVideoService.getPopularShorts());
    }

    // 조회수 증가
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        shortsVideoService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}
