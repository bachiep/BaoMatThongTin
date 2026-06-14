package com.bachiep.sems.dto.response;

import com.bachiep.sems.entity.AuditLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String performedBy;
    private String action;
    private String ipAddress;
    private LocalDateTime timestamp;

    public static AuditLogResponse from(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .performedBy(auditLog.getPerformedBy())
                .action(auditLog.getAction())
                .ipAddress(auditLog.getIpAddress())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
