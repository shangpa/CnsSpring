package com.example.springjwt.shorts;

import com.example.springjwt.User.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortsVideoService {

    private final ShortsVideoRepository shortsVideoRepository;

    public String storeFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/shorts/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String original = file.getOriginalFilename();
        String safeName = (original == null ? "video.mp4" : original).replaceAll("\\s+", "_");
        String fileName = UUID.randomUUID() + "_" + safeName;

        File saveFile = new File(uploadDir, fileName);
        file.transferTo(saveFile);
        return "/uploads/shorts/" + fileName;
    }

    // 👇 새로 추가: 업로드된 URL을 이용해 Shorts 엔티티 등록
    public ShortsVideo createShorts(String title, String videoUrl, boolean isPublic, UserEntity user) {
        if (!StringUtils.hasText(videoUrl)) {
            throw new IllegalArgumentException("videoUrl이 비어 있습니다.");
        }
        ShortsVideo shortsVideo = ShortsVideo.builder()
                .title(title)
                .videoUrl(videoUrl)
                .isPublic(isPublic)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        return shortsVideoRepository.save(shortsVideo);
    }

    // ===== 아래 기존 메서드 유지 =====
    public ShortsVideo uploadVideo(MultipartFile file, String title, boolean isPublic, UserEntity user) throws IOException {
        String videoUrl = storeFile(file); // ← 내부적으로 재사용
        return createShorts(title, videoUrl, isPublic, user);
    }

    // 최신순
    public List<ShortsVideo> getLatestShorts() {
        return shortsVideoRepository.findTop10ByIsPublicTrueOrderByCreatedAtDesc();
    }

    // 인기순
    public List<ShortsVideo> getPopularShorts() {
        return shortsVideoRepository.findTop10ByIsPublicTrueOrderByViewCountDesc();
    }

    //랜덤조회
    public List<ShortsListDto> getRandomSimple(int size) {
        int limit = Math.max(1, size);
        return shortsVideoRepository.findRandomSimple(limit).stream().map(ShortsListDto::from).toList();
    }

    // 조회수 증가
    public void increaseViewCount(Long shortsId) {
        ShortsVideo shorts = shortsVideoRepository.findById(shortsId)
                .orElseThrow(() -> new RuntimeException("숏츠 없음"));
        shorts.setViewCount(shorts.getViewCount() + 1);
        shortsVideoRepository.save(shorts);
    }

    //랜덤시드
    public List<ShortsListDto> getRandomBySeed(String seed, int page, int size) {
        int limit  = Math.max(1, size);          // size 0 방지
        int offset = Math.max(0, page) * limit;  // page 0 → 0, page 1 → limit

        String s = (seed == null || seed.isBlank()) ? "default" : seed;

        var rows = shortsVideoRepository.findRandomBySeedPositional(s, offset, limit);

        System.out.println("[randomBySeed] seed=" + s +
                ", page=" + page + ", size=" + size +
                ", offset=" + offset + ", fetched=" + rows.size());

        return rows.stream().map(ShortsListDto::from).toList();
    }

}
