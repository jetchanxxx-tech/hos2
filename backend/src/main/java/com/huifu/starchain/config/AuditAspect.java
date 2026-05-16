package com.huifu.starchain.config;

import com.huifu.starchain.entity.AuditLog;
import com.huifu.starchain.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 自动审计切面：对 @RequestMapping 写操作自动记录 AuditLog
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditLogRepository auditLogRepo;

    public AuditAspect(AuditLogRepository auditLogRepo) { this.auditLogRepo = auditLogRepo; }

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object auditWriteOps(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        String action = jp.getSignature().getName();
        String resource = jp.getTarget().getClass().getSimpleName().replace("Controller", "");
        Object result = null;
        String errorMsg = null;

        try {
            result = jp.proceed();
            return result;
        } catch (Throwable t) {
            errorMsg = t.getMessage();
            throw t;
        } finally {
            try {
                AuditLog al = new AuditLog();
                Long userId = getCurrentUserId();
                al.setUserId(userId);
                al.setAction(deriveAction(resource, action));
                al.setResourceType(resource);
                al.setResult(errorMsg == null ? "SUCCESS" : "FAIL");
                al.setErrorMsg(errorMsg);
                al.setDurationMs((int) (System.currentTimeMillis() - start));

                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest req = attrs.getRequest();
                    al.setRequestUrl(req.getRequestURI());
                    al.setRequestMethod(req.getMethod());
                    al.setIpAddress(req.getRemoteAddr());
                    al.setUserAgent(req.getHeader("User-Agent"));
                }
                auditLogRepo.save(al);
            } catch (Exception e) {
                log.debug("Audit logging failed: {}", e.getMessage());
            }
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long) {
                return (Long) auth.getPrincipal();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String deriveAction(String resource, String method) {
        if (method.contains("create") || method.contains("register")) return "CREATE";
        if (method.contains("update") || method.contains("edit") || method.contains("change")) return "UPDATE";
        if (method.contains("delete") || method.contains("remove") || method.contains("dissolve")) return "DELETE";
        return method.toUpperCase();
    }
}
