package kz.don.auth.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_service_name", columnList = "service_name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, READ, etc.

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType; // ORDER, PRODUCT, PAYMENT, etc.

    @Column(name = "entity_id", nullable = false, length = 255)
    private String entityId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(columnDefinition = "TEXT")
    private String details; // JSON string with additional info

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IPv4 or IPv6

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "service_name", length = 100)
    private String serviceName; // e.g., "python-business-service"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}