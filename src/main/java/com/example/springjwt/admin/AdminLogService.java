package com.example.springjwt.admin;

import com.example.springjwt.admin.log.AdminLog;
import com.example.springjwt.admin.log.AdminLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AdminLogService {
    private final AdminLogRepository adminLogRepository;

    public void logAdminAction(String adminUsername, String action, String targetType, int targetId, String reason) {
        AdminLog log = new AdminLog();
        log.setAdminUsername(adminUsername);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId((long) targetId);
        log.setReason(reason);
        log.setCreatedAt(LocalDateTime.now());
        adminLogRepository.save(log);
    }
}
