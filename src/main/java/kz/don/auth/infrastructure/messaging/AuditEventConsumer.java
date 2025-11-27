package kz.don.auth.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.don.auth.domain.entity.AuditLog;
import kz.don.auth.domain.repository.AuditLogRepository;
import kz.don.auth.web.dto.request.AuditLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

//    @KafkaListener(
//            topics = "${kafka.topic.audit-events:audit-events}",
//            groupId = "${spring.kafka.consumer.group-id:audit-service}",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
    @Transactional
    public void consumeAuditEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        log.debug("Received message from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            // Manual JSON deserialization using ObjectMapper
            AuditLogRequest request = objectMapper.readValue(message, AuditLogRequest.class);

            AuditLog auditLog = AuditLog.builder()
                    .userId(request.getUserId())
                    .action(request.getAction())
                    .entityType(request.getEntityType())
                    .timestamp(parseTimestamp(request.getTimestamp()))
                    .ipAddress(request.getIpAddress())
                    .serviceName(request.getServiceName())
                    .build();

            auditLogRepository.save(auditLog);

            log.info("Successfully processed audit event: action={}, entityType={}, userId={}, service={}",
                    auditLog.getAction(),
                    auditLog.getEntityType(),
                    auditLog.getUserId(),
                    auditLog.getServiceName());

        } catch (Exception e) {
            log.error("Error processing audit event from topic {}, partition {}, offset {}: {}",
                    topic, partition, offset, e.getMessage(), e);
            throw new RuntimeException("Failed to process audit event", e);
        }
    }

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
        } catch (Exception e) {
            log.error("Failed to serialize details: {}", e.getMessage());
            return null;
        }
    }
}