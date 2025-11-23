package kz.don.auth.envers;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        AuditEnversInfo auditEnversInfo = (AuditEnversInfo) revisionEntity;

        // Set username from Spring Security context
        String username = getCurrentUsername();
        auditEnversInfo.setUsername(username);

        // Set IP address
        String ipAddress = getClientIPAddress();
        auditEnversInfo.setIpAddress(ipAddress);

        // Set the HTTP method as operation type
        String operationType = getHttpMethod();
        auditEnversInfo.setOperationType(operationType);
    }

    private String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof UserDetails) {
                        return ((UserDetails) authentication.getPrincipal()).getUsername();
                    } else {
                        return authentication.getPrincipal().toString();
                    }
                })
                .orElse("SYSTEM");
    }

    private String getClientIPAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getHttpMethod() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return request.getMethod(); // GET, POST, PUT, DELETE, etc.
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}