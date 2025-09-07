package com.example.springjwt.shorts;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserService;
import com.example.springjwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
   /* // 파일만 업로드
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
    }*/

    // 최종 등록
    @PostMapping("/register")
    public ResponseEntity<Long> registerShorts(
            @RequestBody RecipeShortCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            if (userDetails == null) return ResponseEntity.status(401).build();
            if (request.getTitle() == null || request.getTitle().isBlank()
                    || request.getVideoUrl() == null || request.getVideoUrl().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            UserEntity user = userDetails.getUserEntity();

            ShortsVideo saved = shortsVideoService.createShorts(
                    request.getTitle(),
                    request.getVideoUrl(),
                    request.getThumbnailUrl(),
                    request.isPublic(),
                    user
            );
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
    /*
    랜덤재생
    @GetMapping("/random")
    public ResponseEntity<List<ShortsListDto>> randomSimple(
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(shortsVideoService.getRandomSimple(size));
    }*/


    //랜덤시드 재생
    @GetMapping("/random")
    public ResponseEntity<List<ShortsListDto>> randomBySeed(
            @RequestParam String seed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserEntity user = userDetails.getUserEntity(); // 필요 시 현재 로그인 유저 확인 가능
        var list = shortsVideoService.getRandomBySeed(seed, page, size);
        System.out.println("[/api/shorts/random] return size=" + list.size());
        return ResponseEntity.ok(shortsVideoService.getRandomBySeed(seed, page, size));
    }

    //유저별정렬
    @GetMapping("/{userId}")
    public ResponseEntity<ShortsUserVideoListResponse> getUserShorts(
            @PathVariable int userId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        // 정렬 키 매핑
        Sort sortObj = switch (sort) {
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "date"  -> Sort.by(Sort.Direction.ASC,  "createdAt");   // 오래된→최신
            default      -> Sort.by(Sort.Direction.DESC, "createdAt");   // latest: 최신→오래된
        };

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sortObj);
        var pageResult = shortsVideoRepository.findByUser_IdAndIsPublicTrue(userId, pageable);

        var list = pageResult.getContent().stream()
                .map(ShortsUserVideoDto::from)
                .toList();

        return ResponseEntity.ok(new ShortsUserVideoListResponse(list));
    }

    // 랜덤 3개(레시피 탭)
    @GetMapping("/random3")
    public ResponseEntity<List<ShortsCardDto>> random3() {
        return ResponseEntity.ok(shortsVideoService.getRandom3Cards());
    }
}
