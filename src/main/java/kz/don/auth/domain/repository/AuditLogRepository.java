package kz.don.auth.domain.repository;

import kz.don.auth.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType, String entityId);

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            Instant startTime, Instant endTime);

    Page<AuditLog> findByServiceNameOrderByTimestampDesc(
            String serviceName, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
            "(:endTime IS NULL OR a.timestamp <= :endTime) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogs(
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :timestamp")
    int deleteByTimestampBefore(@Param("timestamp") Instant timestamp);

    long countByUserId(UUID userId);

    long countByEntityTypeAndEntityId(String entityType, String entityId);
}