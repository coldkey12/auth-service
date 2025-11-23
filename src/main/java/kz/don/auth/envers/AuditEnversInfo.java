package kz.don.auth.envers;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "audit_envers_info")
@RevisionEntity(AuditRevisionListener.class)
@Data
public class AuditEnversInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Integer id;

    @RevisionTimestamp
    private Long timestamp;

    // Add operation type field
    @Column(name = "operation_type")
    private String operationType;

    // Add username who performed the operation
    private String username;

    // Optional: Add IP address
    @Column(name = "ip_address")
    private String ipAddress;

    @Transient
    public String getOperationTypeAsString() {
        // This would need to be set from the revision listener
        return operationType;
    }

    // Or get it from the revision type in queries
    @Transient
    public String getRevisionTypeAsString(org.hibernate.envers.RevisionType revisionType) {
        return switch (revisionType) {
            case ADD -> "INSERT";
            case MOD -> "UPDATE";
            case DEL -> "DELETE";
        };
    }
}