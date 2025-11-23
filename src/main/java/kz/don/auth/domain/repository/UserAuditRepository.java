package kz.don.auth.domain.repository;

import kz.don.auth.domain.entity.User;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Repository
public class UserAuditRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object[]> getUserRevisionsWithOperation(UUID userId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(User.class, false, true)
                .add(AuditEntity.id().eq(userId))
                .addOrder(AuditEntity.revisionNumber().asc());

        return query.getResultList();
    }

    public List<Object[]> findRevisionsByOperationType(String operationType) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        return auditReader.createQuery()
                .forRevisionsOfEntity(User.class, false, true)
                .add(AuditEntity.revisionType().eq(getRevisionType(operationType)))
                .getResultList();
    }

    private org.hibernate.envers.RevisionType getRevisionType(String operationType) {
        return switch (operationType.toUpperCase()) {
            case "INSERT" -> org.hibernate.envers.RevisionType.ADD;
            case "UPDATE" -> org.hibernate.envers.RevisionType.MOD;
            case "DELETE" -> org.hibernate.envers.RevisionType.DEL;
            default -> null;
        };
    }
}