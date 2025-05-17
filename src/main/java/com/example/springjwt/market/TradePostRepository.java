package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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


}
