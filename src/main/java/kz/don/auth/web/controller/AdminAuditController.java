package kz.don.auth.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.don.auth.application.service.AuditLogService;
import kz.don.auth.web.dto.response.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('AUTHORITY')")
@RequiredArgsConstructor
@Tag(name = "Admin Audit Controller", description = "Administrative operations for audit log management")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Get all audit logs", description = "Retrieves paginated audit logs")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAllAuditLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs(pageable));
    }

    @Operation(summary = "Get user audit logs", description = "Retrieves audit logs for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getUserAuditLogs(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.getUserAuditLogs(userId, pageable));
    }

    @Operation(summary = "Get entity audit logs", description = "Retrieves audit logs for a specific entity")
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable String entityId
    ) {
        return ResponseEntity.ok(auditLogService.getEntityAuditLogs(entityType, entityId));
    }

    @Operation(summary = "Search audit logs", description = "Search audit logs with multiple filters")
    @GetMapping("/search")
    public ResponseEntity<Page<AuditLogResponse>> searchAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.searchAuditLogs(
                userId, action, entityType, startTime, endTime, pageable));
    }

    @Operation(summary = "Get audit logs by service", description = "Retrieves audit logs from a specific service")
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByService(
            @PathVariable String serviceName,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByService(serviceName, pageable));
    }
}