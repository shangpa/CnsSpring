package com.example.springjwt.admin.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    @Query("SELECT l FROM AdminLog l WHERE l.targetType = 'USER' AND l.targetId = :userId AND l.action = 'BLOCK_USER' ORDER BY l.createdAt DESC")
    List<AdminLog> findRecentUserBlocks(@Param("userId") int userId);

}