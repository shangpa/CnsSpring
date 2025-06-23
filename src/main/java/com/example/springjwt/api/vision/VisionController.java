package com.example.springjwt.api.vision;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vision")
public class VisionController {

    private final VisionAnalyzeService visionAnalyzeService;

    @PostMapping("/analyze")
    public ResponseEntity<List<String>> analyzeImage(
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails // ✅ 그대로 사용
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }
        List<String> savedIngredients = visionAnalyzeService.analyzeOnly(image);
        return ResponseEntity.ok(savedIngredients);
    }
}
