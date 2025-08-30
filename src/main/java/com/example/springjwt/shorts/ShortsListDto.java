package com.example.springjwt.shorts;

public record ShortsListDto(
        Long id,
        String title,
        String videoUrl,
        int viewCount,
        int likeCount,
        String userName
) {
    public static ShortsListDto from(ShortsVideo v) {
        String name = (v.getUser() != null) ? v.getUser().getUsername() : "user";
        return new ShortsListDto(
                v.getId(),
                v.getTitle(),
                v.getVideoUrl(),
                v.getViewCount(),
                v.getLikeCount(),
                name
        );
    }
}
