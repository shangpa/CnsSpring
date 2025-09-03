package com.example.springjwt.tradepost;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.admin.dto.BoardMonthlyStatsDTO;
import com.example.springjwt.admin.dto.TradePostDetailResponseDTO;
import com.example.springjwt.admin.dto.TradePostListResponseDTO;
import com.example.springjwt.admin.log.AdminLogService;
import com.example.springjwt.chat.ChatMessageRepository;
import com.example.springjwt.chat.ChatMessageService;
import com.example.springjwt.chat.ChatRoom;
import com.example.springjwt.chat.ChatRoomRepository;
import com.example.springjwt.point.PointActionType;
import com.example.springjwt.point.PointService;
import com.example.springjwt.review.TradePost.TpReviewRepository;
import com.example.springjwt.tradepost.saved.SavedTradePostRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.springjwt.util.DistanceUtil;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class TradePostService {

    private final TradePostRepository tradePostRepository;
    private final UserRepository userRepository;
    private final SavedTradePostRepository savedTradePostRepository;
    private final AdminLogService adminLogService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TpReviewRepository tpReviewRepository;
    private final PointService pointService;

    public TradePost create(TradePostDTO dto, String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        TradePost tradePost = dto.toEntity();
        tradePost.setUser(user);
        tradePost.setStatus(0);

        // location에서 위도, 경도 분리하여 저장
        String location = dto.getLocation();
        if (location != null && location.contains(",")) {
            try {
                String[] parts = location.split(",");
                double latitude = Double.parseDouble(parts[0].trim());
                double longitude = Double.parseDouble(parts[1].trim());
                tradePost.setLatitude(latitude);
                tradePost.setLongitude(longitude);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("위치 형식이 잘못되었습니다: " + location);
            }
        }

        return tradePostRepository.save(tradePost);
    }

    public TradePostDTO getTradePostById(Long id, String username) {
        TradePost tradePost = tradePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래글이 존재하지 않습니다. ID=" + id));

        if (username != null) {
            UserEntity user = userRepository.findByUsername(username);
            if (user != null && user.getLatitude() != null && user.getLongitude() != null
                    && tradePost.getLatitude() != null && tradePost.getLongitude() != null) {
                double distance = DistanceUtil.calculateDistance(
                        user.getLatitude(), user.getLongitude(),
                        tradePost.getLatitude(), tradePost.getLongitude()
                );
                return TradePostDTO.fromEntityWithDistance(tradePost, distance);
            }
        }

        return TradePostDTO.fromEntity(tradePost);
    }

    public TradePost completeTradePost(Long id) {
        TradePost tradePost = tradePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래글이 존재하지 않습니다. ID=" + id));
        tradePost.setStatus(TradePost.STATUS_COMPLETED);
        return tradePostRepository.save(tradePost);
    }

    public List<TradePostSimpleResponseDTO> getMyTradePosts(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        List<TradePost> myPosts = tradePostRepository.findByUser(user);
        return myPosts.stream()
                .map(TradePostSimpleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TradePostDTO> getAllTradePosts() {
        return tradePostRepository.findAll().stream()
                .map(TradePostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TradePostDTO> getTradePostsByCategory(String category) {
        return tradePostRepository.findByCategory(category).stream()
                .map(TradePostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TradePostDTO> searchTradePosts(String keyword) {
        List<TradePost> posts = tradePostRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        return posts.stream()
                .map(TradePostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 위치 기반 거래글 조회 기능
    public List<TradePostDTO> getNearbyTradePosts(String username, double distanceKm) {
        UserEntity user = userRepository.findByUsername(username);

        // 위치 정보가 없거나 비로그인한 경우 전체 최신순 정렬
        if (user == null || user.getLatitude() == null || user.getLongitude() == null) {
            return tradePostRepository.findAllByOrderByUpdatedAtDesc().stream()
                    .map(TradePostDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        double userLat = user.getLatitude();
        double userLon = user.getLongitude();

        return tradePostRepository.findAll().stream()
                .filter(post -> post.getLatitude() != null && post.getLongitude() != null)
                .map(post -> {
                    double distance = calculateDistance(userLat, userLon, post.getLatitude(), post.getLongitude());
                    return new AbstractMap.SimpleEntry<>(post, distance);
                })
                .filter(entry -> entry.getValue() <= distanceKm)
                .map(entry -> TradePostDTO.fromEntityWithDistance(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<TradePostDTO> getTradePostsSortedByDistance(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null || user.getLatitude() == null || user.getLongitude() == null) {
            return tradePostRepository.findAllByOrderByUpdatedAtDesc().stream()
                    .map(TradePostDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        double userLat = user.getLatitude();
        double userLon = user.getLongitude();

        List<TradePost> allPosts = tradePostRepository.findAll().stream()
                .filter(post -> post.getLatitude() != null && post.getLongitude() != null)
                .sorted(Comparator.comparingDouble(post ->
                        DistanceUtil.calculateDistance(userLat, userLon, post.getLatitude(), post.getLongitude())
                ))
                .collect(Collectors.toList());

        return allPosts.stream()
                .map(TradePostDTO::fromEntity)
                .collect(Collectors.toList());
    }

    //카테고리 + 거리순 필터링
    public List<TradePostDTO> getNearbyByCategory(String username, double distanceKm, String category) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null || user.getLatitude() == null || user.getLongitude() == null) {
            return tradePostRepository.findAllByOrderByUpdatedAtDesc().stream()
                    .map(TradePostDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        double userLat = user.getLatitude();
        double userLon = user.getLongitude();

        return tradePostRepository.findAll().stream()
                .filter(post ->
                        post.getCategory().equals(category) &&
                                post.getLatitude() != null && post.getLongitude() != null
                )
                .map(post -> {
                    double distance = calculateDistance(userLat, userLon, post.getLatitude(), post.getLongitude());
                    return new AbstractMap.SimpleEntry<>(post, distance);
                })
                .filter(entry -> entry.getValue() <= distanceKm)
                .map(entry -> TradePostDTO.fromEntityWithDistance(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<TradePostDTO> getNearbyPostsByMultipleCategories(UserEntity user, double distanceKm, List<String> categories) {
        if (user == null || user.getLatitude() == null || user.getLongitude() == null) {
            // 위치 정보 없으면 전체 최신순 + 카테고리 필터만 적용
            return tradePostRepository.findAllByOrderByUpdatedAtDesc().stream()
                    .filter(post -> categories.contains(post.getCategory()))
                    .map(TradePostDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        double userLat = user.getLatitude();
        double userLng = user.getLongitude();

        List<TradePost> posts = tradePostRepository.findAll().stream()
                .filter(post -> post.getLatitude() != null && post.getLongitude() != null)
                .filter(post -> DistanceUtil.calculateDistance(userLat, userLng, post.getLatitude(), post.getLongitude()) <= distanceKm)
                .filter(post -> categories.contains(post.getCategory()))
                .collect(Collectors.toList());

        return posts.stream()
                .map(post -> TradePostDTO.fromEntityWithDistance(post,
                        DistanceUtil.calculateDistance(userLat, userLng, post.getLatitude(), post.getLongitude())))
                .collect(Collectors.toList());
    }

    public List<TradePostSimpleResponseDTO> getTop3PopularTradePosts() {
        Pageable pageable = PageRequest.of(0, 3);
        List<TradePost> topPosts = tradePostRepository.findTop3ByOrderByViewCountDesc(pageable);

        return topPosts.stream()
                .map(TradePostSimpleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(Long postId) {
        TradePost post = tradePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("거래글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
    }

    @Transactional
    public TradePost completeTradePost(Long postId, Long buyerId) {
        TradePost post = tradePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("거래글이 존재하지 않습니다."));

        if (post.getStatus() == TradePost.STATUS_COMPLETED) {
            throw new IllegalStateException("이미 거래가 완료된 게시글입니다.");
        }

        UserEntity buyer = userRepository.findById(Math.toIntExact(buyerId))
                .orElseThrow(() -> new IllegalArgumentException("구매자가 존재하지 않습니다."));

        UserEntity seller = post.getUser();
        int price = post.getPrice();

        // 1. 포인트 차감 (구매자)
        pointService.usePoint(buyer, price, "거래 지출 - " + post.getTitle());

        // 🟢 판매자 포인트 적립
        pointService.addPoint(seller, PointActionType.TRADE_COMPLETE, price, "거래 수익 - " + post.getTitle());

        // 3. 거래 상태 변경
        post.setStatus(TradePost.STATUS_COMPLETED);

        // 4. 구매자 정보 저장 (필드가 있다면)
        post.setBuyer(buyer); // TradePost에 buyer 필드 필요

        return tradePostRepository.save(post);
    }
    public List<TradePostSimpleResponseDTO> getMyPurchasedPosts(String username) {
        UserEntity user = userRepository.findByUsername(username);
        List<TradePost> posts = tradePostRepository.findByBuyerAndStatus(user, TradePost.STATUS_COMPLETED);
        return posts.stream()
                .map(TradePostSimpleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    //특정 유저 포스트 가져오기
    public List<TradePostSimpleResponseDTO> getPostsByUsername(String username) {
        return tradePostRepository.findByUser_Username(username).stream()
                .map(TradePostSimpleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    //특정 유저 포스트(상태) 가져오기
    public List<TradePostSimpleResponseDTO> getPostsByUsernameAndStatus(String username, int status) {
        return tradePostRepository.findByUser_UsernameAndStatus(username, status).stream()
                .map(TradePostSimpleResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BoardMonthlyStatsDTO> countTradePostMonthly(LocalDateTime startDate) {
        List<Object[]> raw = tradePostRepository.countTradePostMonthlyRaw(startDate);
        return raw.stream()
                .map(row -> new BoardMonthlyStatsDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    public List<BoardMonthlyStatsDTO> countFreeTradePostMonthly(LocalDateTime startDate) {
        List<Object[]> raw = tradePostRepository.countFreeTradePostMonthlyRaw(startDate);
        return raw.stream()
                .map(row -> new BoardMonthlyStatsDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    public Page<TradePostListResponseDTO> getTradePosts(int page, int size, Integer status, String sortBy, String keyword) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "updatedAt";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<TradePost> tradePosts;

        if (keyword != null && !keyword.isBlank()) {
            tradePosts = tradePostRepository.findByStatusAndTitleKeyword(status, keyword, pageable);
        } else if (status == null) {
            tradePosts = tradePostRepository.findAll(pageable);
        } else {
            tradePosts = tradePostRepository.findByStatus(status, pageable);
        }

        return tradePosts.map(tp -> new TradePostListResponseDTO(
                tp.getTradePostId(),
                tp.getUser().getUsername(),
                tp.getTitle(),
                tp.getCreatedAt(),
                tp.getCategory(),
                tp.getStatus()
        ));
    }



    public TradePostDetailResponseDTO getTradePostDetail(Long postId) {
        TradePost tradePost = tradePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 거래글이 존재하지 않습니다."));

        // 이미지 JSON -> 리스트 변환
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> imageUrls;
        try {
            imageUrls = objectMapper.readValue(tradePost.getImageUrls(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            imageUrls = List.of();
        }

        int chatCount = chatRoomRepository.countByTradePostId(postId);

        return new TradePostDetailResponseDTO(
                tradePost.getTradePostId(),
                tradePost.getUser().getUsername(),
                tradePost.getTitle(),
                tradePost.getDescription(),
                tradePost.getCreatedAt(),
                imageUrls,
                tradePost.getLocation(),
                chatCount,
                tradePost.getViewCount(),
                tradePost.getStatus()
        );
    }


    public void deletePostByAdmin(Long postId, String adminUsername, String reason) {
        TradePost post = tradePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("거래글이 존재하지 않습니다."));

        // 1. 연관 채팅 메시지 먼저 삭제
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByPost(post);
        for (ChatRoom room : chatRooms) {
            chatMessageRepository.deleteAllByRoomKey(room.getRoomKey());
        }

        // 2. 채팅방 삭제
        chatRoomRepository.deleteAll(chatRooms);

        // 3. 찜 삭제
        savedTradePostRepository.deleteAllByTradePost(post);

        // 4. 리뷰 삭제
        tpReviewRepository.deleteAllByTradePost(post);

        // 5. 거래글 삭제
        tradePostRepository.delete(post);

        // 6. 관리자 로그 기록
        adminLogService.logAdminAction(
                adminUsername,
                "DELETE_POST",
                "TRADE_POST",
                postId,
                reason
        );
    }
    public UserProfileResponseDTO getUserProfile(UserEntity user) {
        System.out.println("🔥 조회된 유저: " + user.getUsername()); // null 아닌지 확인

        int reviewCount = tpReviewRepository.countByUser(user);
        Double avgRating = tpReviewRepository.avgRatingByUser((long) user.getId());
        double rating = (avgRating != null) ? Math.round(avgRating * 10) / 10.0 : 0.0;
        int transactionCount = tradePostRepository.countByUser(user);

        UserProfileResponseDTO dto = new UserProfileResponseDTO(
                user.getUsername(),
                rating,
                reviewCount,
                transactionCount
        );

        System.out.println("📦 프로필 DTO: " + dto); // 로그 출력

        return dto;
    }


}
