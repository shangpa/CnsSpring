package com.example.springjwt.shorts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShortsVideoRepository extends JpaRepository<ShortsVideo, Long> {
    List<ShortsVideo> findTop10ByIsPublicTrueOrderByCreatedAtDesc(); // 최신순
    List<ShortsVideo> findTop10ByIsPublicTrueOrderByViewCountDesc(); // 인기순
    int countByUser_Id(int userId);

    @Query(value = "SELECT * FROM shorts_video WHERE is_public = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<ShortsVideo> findRandomSimple(@Param("limit") int limit);

    // ✅ 위치 파라미터 사용 (MySQL): seed, offset, limit
    @Query(
            value = """
            SELECT * FROM shorts_video
            WHERE is_public = true
            ORDER BY CRC32(CONCAT(?1, id))
            LIMIT ?2, ?3
            """,
            nativeQuery = true
    )
    List<ShortsVideo> findRandomBySeedPositional(
            String seed,   // ?1
            int offset,    // ?2
            int limit      // ?3
    );

}
