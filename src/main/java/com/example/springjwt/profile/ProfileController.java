package com.example.springjwt.profile;

import com.example.springjwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}/summary")
    public ResponseEntity<ProfileSummaryResponse> getSummary(
            @PathVariable int userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int requesterId = userDetails.getUserEntity().getId(); // int 가정
        return ResponseEntity.ok(profileService.getSummary(userId, requesterId));
    }

    @PostMapping("/{userId}/follow-toggle")
    public ResponseEntity<String> toggleFollow(
            @PathVariable int userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int requesterId = userDetails.getUserEntity().getId();
        boolean nowFollowing = profileService.toggleFollow(requesterId, userId);
        return ResponseEntity.ok(nowFollowing ? "팔로우했습니다." : "팔로우를 취소했습니다.");
    }
}
