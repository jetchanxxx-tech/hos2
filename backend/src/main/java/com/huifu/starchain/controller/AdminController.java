package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;
import com.huifu.starchain.service.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")

public class AdminController {

    private final UserRepository userRepo;
    private final ServicePackageService pkgService;
    private final AuditLogRepository auditLogRepo;
    private final HospitalGatewaySyncRepository syncRepo;
    private final KnowledgeArticleRepository knowledgeRepo;

    public AdminController(UserRepository userRepo, ServicePackageService pkgService, AuditLogRepository auditLogRepo, HospitalGatewaySyncRepository syncRepo, KnowledgeArticleRepository knowledgeRepo) { this.userRepo = userRepo; this.pkgService = pkgService; this.auditLogRepo = auditLogRepo; this.syncRepo = syncRepo; this.knowledgeRepo = knowledgeRepo; }

    // ---- Users ----
    @GetMapping("/users")
    public ApiResponse<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role) {
        if (role != null) {
            var pg = userRepo.findByRole(User.UserRole.valueOf(role), PageRequest.of(page - 1, size));
            return ApiResponse.ok(PageResult.of(pg.getContent(), pg.getTotalElements(), page, size));
        }
        var pg = userRepo.findAll(PageRequest.of(page - 1, size));
        return ApiResponse.ok(PageResult.of(pg.getContent(), pg.getTotalElements(), page, size));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<User> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(User.UserStatus.valueOf(status));
            userRepo.save(user);
        }
        return ApiResponse.ok(user);
    }

    @PutMapping("/users/{id}/role")
    public ApiResponse<User> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            user.setRole(User.UserRole.valueOf(role));
            userRepo.save(user);
        }
        return ApiResponse.ok(user);
    }

    // ---- Packages ----
    @PostMapping("/packages")
    public ApiResponse<ServicePackage> createPackage(@RequestBody ServicePackage pkg) {
        return ApiResponse.ok(pkgService.createPackage(pkg));
    }

    @PutMapping("/packages/{id}")
    public ApiResponse<ServicePackage> updatePackage(@PathVariable Long id, @RequestBody ServicePackage pkg) {
        return ApiResponse.ok(pkgService.updatePackage(id, pkg));
    }

    // ---- Audit Logs ----
    @GetMapping("/audit-logs")
    public ApiResponse<PageResult<AuditLog>> auditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long userId) {
        var pg = userId != null
                ? auditLogRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size))
                : auditLogRepo.findAll(PageRequest.of(page - 1, size));
        return ApiResponse.ok(PageResult.of(pg.getContent(), pg.getTotalElements(), page, size));
    }

    // ---- Sync Status ----
    @GetMapping("/sync-status")
    public ApiResponse<?> syncStatus() {
        return ApiResponse.ok(syncRepo.findBySyncStatusOrderByCreatedAtAsc("FAILED"));
    }

    @GetMapping("/sync-status/pending")
    public ApiResponse<?> pendingSync() {
        return ApiResponse.ok(syncRepo.findBySyncStatusOrderByCreatedAtAsc("PENDING"));
    }

    // ---- Knowledge Base ----
    @GetMapping("/knowledge")
    public ApiResponse<PageResult<KnowledgeArticle>> listKnowledge(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pg = knowledgeRepo.findAll(PageRequest.of(page - 1, size));
        return ApiResponse.ok(PageResult.of(pg.getContent(), pg.getTotalElements(), page, size));
    }

    @PostMapping("/knowledge")
    public ApiResponse<KnowledgeArticle> createKnowledge(@RequestBody KnowledgeArticle article) {
        article.setStatus("PUBLISHED");
        return ApiResponse.ok(knowledgeRepo.save(article));
    }

    @PutMapping("/knowledge/{id}")
    public ApiResponse<KnowledgeArticle> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeArticle article) {
        var existing = knowledgeRepo.findById(id).orElse(null);
        if (existing == null) return ApiResponse.error(40400, "知识文章不存在");
        existing.setQuestion(article.getQuestion());
        existing.setAnswer(article.getAnswer());
        existing.setCategory(article.getCategory());
        existing.setTags(article.getTags());
        existing.setStatus(article.getStatus());
        return ApiResponse.ok(knowledgeRepo.save(existing));
    }

    // ---- Stats ----
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(Map.of(
                "totalUsers", userRepo.count(),
                "activeResidents", userRepo.countByRole(User.UserRole.RESIDENT),
                "butlers", userRepo.countByRole(User.UserRole.BUTLER_MEDICAL) + userRepo.countByRole(User.UserRole.BUTLER_SERVICE)
        ));
    }
}
