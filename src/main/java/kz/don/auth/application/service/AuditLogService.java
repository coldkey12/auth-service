package kz.don.auth.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.don.auth.domain.entity.AuditLog;
import kz.don.auth.domain.repository.AuditLogRepository;
import kz.don.auth.web.dto.request.AuditLogRequest;
import kz.don.auth.web.dto.response.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log an external action from another microservice
     */
    @Transactional
    public void logExternalAction(AuditLogRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(request.getUserId())
                    .action(request.getAction())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .timestamp(parseTimestamp(request.getTimestamp()))
                    .details(serializeDetails(request.getDetails()))
                    .ipAddress(request.getIpAddress())
                    .userAgent(request.getUserAgent())
                    .serviceName(request.getServiceName())
                    .build();

            auditLogRepository.save(auditLog);

            log.info("Audit log created: action={}, entityType={}, userId={}, service={}",
                    request.getAction(),
                    request.getEntityType(),
                    request.getUserId(),
                    request.getServiceName());

        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create audit log", e);
        }
    }

    /**
     * Get audit logs for a specific user
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserAuditLogs(UUID userId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Get audit logs for a specific entity
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getEntityAuditLogs(String entityType, String entityId) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                entityType, entityId);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all audit logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Get audit logs by action type
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByAction(String action, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Get audit logs within a time range
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByTimeRange(Instant startTime, Instant endTime) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
                startTime, endTime);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by service name
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByService(String serviceName, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByServiceNameOrderByTimestampDesc(
                serviceName, pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Search audit logs with multiple filters
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchAuditLogs(
            UUID userId,
            String action,
            String entityType,
            Instant startTime,
            Instant endTime,
            Pageable pageable) {

        Page<AuditLog> logs = auditLogRepository.searchAuditLogs(
                userId, action, entityType, startTime, endTime, pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Delete old audit logs (for cleanup jobs)
     */
    @Transactional
    public int deleteAuditLogsOlderThan(Instant timestamp) {
        int deletedCount = auditLogRepository.deleteByTimestampBefore(timestamp);
        log.info("Deleted {} audit logs older than {}", deletedCount, timestamp);
        return deletedCount;
    }

    // Helper methods

    private Instant parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return Instant.now();
        }
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}, using current time", timestamp);
            return Instant.now();
        }
    }

    private String serializeDetails(Object details) {
        if (details == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize details: {}", e.getMessage());
            return null;
        }
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .timestamp(auditLog.getTimestamp())
                .details(deserializeDetails(auditLog.getDetails()))
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .serviceName(auditLog.getServiceName())
                .build();
    }

    private Object deserializeDetails(String details) {
        if (details == null) {
            return null;
        }
        try {
            return objectMapper.readValue(details, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize details: {}", e.getMessage());
            return details; // Return as string if parsing fails
        }
    }
}