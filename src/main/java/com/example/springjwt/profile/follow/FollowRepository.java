package com.example.springjwt.profile.follow;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollower_IdAndFollowing_Id(int followerId, int followingId);
    long countByFollowing_Id(int followingId); // 팔로워 수
    long countByFollower_Id(int followerId);   // 팔로잉 수
    void deleteByFollower_IdAndFollowing_Id(int followerId, int followingId);
    boolean existsByFollowerIdAndFollowingId(int followerId, int followingId);
}
