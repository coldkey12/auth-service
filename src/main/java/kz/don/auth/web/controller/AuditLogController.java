package kz.don.auth.web.controller;

import kz.don.auth.application.service.AuditLogService;
import kz.don.auth.web.dto.request.AuditLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping("/log")
    public ResponseEntity<Void> logAudit(
            @RequestBody AuditLogRequest request,
            @RequestHeader("X-API-Key") String apiKey
    ) {
        // Validate API key
        if (!isValidApiKey(apiKey)) {
            return ResponseEntity.status(401).build();
        }

        auditLogService.logExternalAction(request);
        return ResponseEntity.ok().build();
    }

    private boolean isValidApiKey(String apiKey) {
        // Implement your API key validation
        return "*aGPM?h[+:*NJ!'a?9'wzT{xwXD=?+".equals(apiKey);
    }
}