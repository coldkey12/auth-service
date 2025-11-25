package kz.don.auth.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;
    private UUID userId;
    private String action;
    private String entityType;
    private String entityId;
    private Instant timestamp;
    private Object details; // Can be Map or any JSON structure
    private String ipAddress;
    private String userAgent;
    private String serviceName;
}