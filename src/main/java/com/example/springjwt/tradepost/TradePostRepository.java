package com.example.springjwt.tradepost;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradePostRepository extends JpaRepository<TradePost, Long> {

    List<TradePost> findByUser(UserEntity user);

    List<TradePost> findByCategory(String category);

    List<TradePost> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword
    );

    // 사용자 위치 기준 거리 필터링 (예: 1km 이내)
    @Query(value = """
        SELECT *, 
        (6371 * acos(
            cos(radians(:lat)) * cos(radians(tp.latitude)) * 
            cos(radians(tp.longitude) - radians(:lng)) + 
            sin(radians(:lat)) * sin(radians(tp.latitude))
        )) AS distance
        FROM trade_post tp
        HAVING distance <= :distanceKm
        ORDER BY distance
    """, nativeQuery = true)
    List<TradePost> findNearbyTradePosts(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("distanceKm") double distanceKm
    );

    //카테고리 + 거리순 필터링
    @Query("""
    SELECT t FROM TradePost t 
    WHERE t.latitude IS NOT NULL AND t.longitude IS NOT NULL
    AND t.category = :category
    AND (
        6371 * acos(
            cos(radians(:userLat)) *
            cos(radians(t.latitude)) *
            cos(radians(t.longitude) - radians(:userLon)) +
            sin(radians(:userLat)) *
            sin(radians(t.latitude))
        )
    ) <= :distanceKm
    """)
    List<TradePost> findNearbyByCategory(
            @Param("userLat") double userLat,
            @Param("userLon") double userLon,
            @Param("distanceKm") double distanceKm,
            @Param("category") String category
    );

    @Query("SELECT t FROM TradePost t ORDER BY t.viewCount DESC")
    List<TradePost> findTop3ByOrderByViewCountDesc(Pageable pageable);

    @Query("SELECT t FROM TradePost t ORDER BY t.createdAt DESC")
    List<TradePost> findAllByOrderByCreatedAtDesc();

    List<TradePost> findByBuyerAndStatus(UserEntity buyer, int status);

    List<TradePost> findByUser_Username(String username);
    List<TradePost> findByUser_UsernameAndStatus(String username, int status);

    // 전체 거래글 월별 수
    @Query("""
    SELECT FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m'), COUNT(t)
    FROM TradePost t
    WHERE t.createdAt >= :startDate
    GROUP BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m')
""")
    List<Object[]> countTradePostMonthlyRaw(@Param("startDate") LocalDateTime startDate);

    // 가격이 0원인 거래글 월별 수
    @Query("""
    SELECT FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m'), COUNT(t)
    FROM TradePost t
    WHERE t.createdAt >= :startDate AND t.price = 0
    GROUP BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m')
    ORDER BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m')
""")
    List<Object[]> countFreeTradePostMonthlyRaw(@Param("startDate") LocalDateTime startDate);

    int countByUser(UserEntity user);

    Page<TradePost> findAll(Pageable pageable);

    Page<TradePost> findByStatus(int status, Pageable pageable);

}
