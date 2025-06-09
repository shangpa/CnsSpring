package com.example.springjwt.api.vision;

import com.example.springjwt.User.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal UserEntity user
    ) {
        List<String> savedIngredients = visionAnalyzeService.analyzeAndSave(image, user);
        return ResponseEntity.ok(savedIngredients);
    }
}
