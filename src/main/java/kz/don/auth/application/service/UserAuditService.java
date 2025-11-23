package kz.don.auth.application.service;

import kz.don.auth.domain.entity.User;
import kz.don.auth.domain.repository.UserAuditRepository;
import kz.don.auth.envers.AuditEnversInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditService {

    private final UserAuditRepository userAuditRepository;

    public void displayUserAuditHistory(UUID userId) {
        List<Object[]> revisions = userAuditRepository.getUserRevisionsWithOperation(userId);

        for (Object[] revision : revisions) {
            User user = (User) revision[0];
            AuditEnversInfo revisionInfo = (AuditEnversInfo) revision[1];
            org.hibernate.envers.RevisionType revisionType = (org.hibernate.envers.RevisionType) revision[2];

            System.out.println("Operation: " + revisionType +
                    ", User: " + revisionInfo.getUsername() +
                    ", IP: " + revisionInfo.getIpAddress() +
                    ", Timestamp: " + new Date(revisionInfo.getTimestamp()));
        }
    }
}
