package kz.don.auth.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Action is required")
    private String action; // CREATE, UPDATE, DELETE, READ, etc.

    @NotBlank(message = "Entity type is required")
    private String entityType; // ORDER, PRODUCT, PAYMENT, etc.

    private String timestamp; // ISO format timestamp from Python service

    private String ipAddress;

    private String serviceName; // Name of the service that generated the log
}