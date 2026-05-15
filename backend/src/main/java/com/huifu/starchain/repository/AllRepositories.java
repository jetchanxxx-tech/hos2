package com.huifu.starchain.repository;

import com.huifu.starchain.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByPhoneHash(String phoneHash);
    Optional<User> findByOpenid(String openid);
    Optional<User> findByWecomId(String wecomId);
    Optional<User> findByFamilyIdAndId(Long familyId, Long id);
    List<User> findByFamilyId(Long familyId);
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    long countByRole(User.UserRole role);
    long countByStatus(User.UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since AND u.role = 'RESIDENT'")
    long countNewResidentsSince(LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.familyId IS NOT NULL AND u.role = 'RESIDENT'")
    long countWithFamily();
}

@Repository
interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByInviteCode(String inviteCode);
    Optional<Family> findByPrimaryUserId(Long primaryUserId);

    @Query("SELECT f FROM Family f WHERE f.status = 'ACTIVE'")
    Page<Family> findAllActive(Pageable pageable);
}

@Repository
interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByFamilyId(Long familyId);
    List<FamilyMember> findByUserId(Long userId);
    Optional<FamilyMember> findByFamilyIdAndUserId(Long familyId, Long userId);
    void deleteByFamilyIdAndUserId(Long familyId, Long userId);
    long countByFamilyId(Long familyId);
}

@Repository
interface HealthRecordRepository extends JpaRepository<HealthRecord, Long>, JpaSpecificationExecutor<HealthRecord> {
    Page<HealthRecord> findByUserIdAndIsDeletedFalseOrderByEventDateDesc(Long userId, Pageable pageable);
    Page<HealthRecord> findByUserIdAndRecordTypeAndIsDeletedFalseOrderByEventDateDesc(Long userId, String recordType, Pageable pageable);
    List<HealthRecord> findByUserIdAndAbnormalFlagGreaterThanAndIsDeletedFalseOrderByEventDateDesc(Long userId, Integer abnormalFlag);

    @Query("SELECT hr FROM HealthRecord hr WHERE hr.userId = :userId AND hr.eventDate BETWEEN :from AND :to AND hr.isDeleted = false ORDER BY hr.eventDate ASC")
    List<HealthRecord> findTimelineByDateRange(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT hr.source, COUNT(hr) FROM HealthRecord hr WHERE hr.createdAt >= :since AND hr.isDeleted = false GROUP BY hr.source")
    List<Object[]> countBySourceSince(LocalDateTime since);
}

@Repository
interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByHealthRecordId(Long healthRecordId);
    List<LabReport> findByIndicatorCodeAndIsAbnormalNotOrderByReportDateDesc(String indicatorCode, Integer isAbnormal);

    @Query("SELECT lr FROM LabReport lr WHERE lr.indicatorCode = :code ORDER BY lr.reportDate ASC")
    List<LabReport> findTrendByIndicator(String code);
}

@Repository
interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    Page<ServicePackage> findByStatusOrderBySortOrderAsc(ServicePackage.PkgStatus status, Pageable pageable);
    List<ServicePackage> findByCategoryAndStatus(String category, ServicePackage.PkgStatus status);
    List<ServicePackage> findByStatus(ServicePackage.PkgStatus status);

    @Query("SELECT sp.type, COUNT(sp) FROM ServicePackage sp WHERE sp.status = 'ON_SHELF' GROUP BY sp.type")
    List<Object[]> countByType();
}

@Repository
interface PackageOrderRepository extends JpaRepository<PackageOrder, Long> {
    Optional<PackageOrder> findByOrderNo(String orderNo);
    Page<PackageOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<PackageOrder> findByStatus(String status, Pageable pageable);
    List<PackageOrder> findByUserIdAndStatusAndEndDateAfter(Long userId, String status, LocalDate date);

    @Query("SELECT COALESCE(SUM(po.amount), 0) FROM PackageOrder po WHERE po.status = 'PAID' AND po.paidAt >= :since")
    java.math.BigDecimal sumRevenueSince(LocalDateTime since);

    @Query("SELECT po.packageId, COUNT(po) as cnt, SUM(po.amount) as revenue FROM PackageOrder po WHERE po.status IN ('PAID', 'ACTIVE') AND po.paidAt >= :since GROUP BY po.packageId ORDER BY cnt DESC")
    List<Object[]> salesRankingSince(LocalDateTime since);
}

@Repository
interface BenefitRedemptionRepository extends JpaRepository<BenefitRedemption, Long> {
    List<BenefitRedemption> findByOrderId(Long orderId);
    List<BenefitRedemption> findByUserId(Long userId);
    List<BenefitRedemption> findByUserIdAndBenefitType(Long userId, String benefitType);
    long countByRedeemedByAndRedeemedAtBetween(Long redeemedBy, LocalDateTime from, LocalDateTime to);
}

@Repository
interface FollowupTaskRepository extends JpaRepository<FollowupTask, Long> {
    Page<FollowupTask> findByAssignedButlerIdAndStatusOrderByScheduledDateAsc(Long butlerId, String status, Pageable pageable);
    Page<FollowupTask> findByUserIdOrderByScheduledDateDesc(Long userId, Pageable pageable);
    List<FollowupTask> findByStatusAndScheduledDateBetween(String status, LocalDate from, LocalDate to);
    List<FollowupTask> findByStatusAndScheduledDateBefore(String status, LocalDate date);

    @Query("SELECT COUNT(ft) FROM FollowupTask ft WHERE ft.status = 'COMPLETED' AND ft.completedAt >= :since")
    long countCompletedSince(LocalDateTime since);

    @Query("SELECT COUNT(ft) FROM FollowupTask ft WHERE ft.scheduledDate BETWEEN :from AND :to")
    long countScheduledBetween(LocalDate from, LocalDate to);

    @Query("SELECT ft.status, COUNT(ft) FROM FollowupTask ft GROUP BY ft.status")
    List<Object[]> countByStatus();
}

@Repository
interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionNo(String sessionNo);
    Page<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    Page<ChatSession> findByStatusAndEscalationLevelNotOrderByUpdatedAtAsc(String status, String escalationLevel, Pageable pageable);
    List<ChatSession> findByStatusAndIntentType(String status, String intentType);
}

@Repository
interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderBySeqNoAsc(Long sessionId);
    List<ChatMessage> findByTriggerAlertTrueOrderByCreatedAtDesc();
}

@Repository
interface DashboardCacheRepository extends JpaRepository<DashboardCache, Long> {
    List<DashboardCache> findByMetricNameAndDimensionAndPeriodStartBetweenOrderByCalculatedAtAsc(String metricName, String dimension, LocalDate from, LocalDate to);
    List<DashboardCache> findByDimensionAndPeriodStart(String dimension, LocalDate periodStart);
}

@Repository
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(String resourceType, String resourceId, Pageable pageable);
}

@Repository
interface HospitalGatewaySyncRepository extends JpaRepository<HospitalGatewaySync, Long> {
    Optional<HospitalGatewaySync> findByDataTypeAndExternalId(String dataType, String externalId);
    List<HospitalGatewaySync> findBySyncStatusOrderByCreatedAtAsc(String syncStatus);
    List<HospitalGatewaySync> findByBatchNo(String batchNo);
}

@Repository
interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    Page<KnowledgeArticle> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<KnowledgeArticle> findByCategoryAndStatus(String category, String status, Pageable pageable);

    @Query(value = "SELECT * FROM knowledge_articles WHERE status = 'PUBLISHED' AND MATCH(question, answer) AGAINST(?1 IN NATURAL LANGUAGE MODE) LIMIT ?2", nativeQuery = true)
    List<KnowledgeArticle> searchByFulltext(String keyword, int limit);

    @Query("SELECT ka FROM KnowledgeArticle ka WHERE ka.status = 'PUBLISHED' AND (ka.question LIKE %:keyword% OR ka.title LIKE %:keyword%)")
    List<KnowledgeArticle> searchByKeyword(String keyword, Pageable pageable);
}
