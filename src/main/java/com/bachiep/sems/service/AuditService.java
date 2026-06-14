package com.bachiep.sems.service;

import com.bachiep.sems.entity.AuditLog;
import com.bachiep.sems.dto.response.AuditLogResponse;
import com.bachiep.sems.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String performedBy, String action, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .performedBy(performedBy)
                .action(action)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getAuditLogs(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(safePage, safeSize))
                .map(AuditLogResponse::from);
    }
}
