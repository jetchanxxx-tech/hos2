package com.huifu.starchain.repository;

import com.huifu.starchain.entity.HealthRecord;
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
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long>, JpaSpecificationExecutor<HealthRecord> {
    Page<HealthRecord> findByUserIdAndIsDeletedFalseOrderByEventDateDesc(Long userId, Pageable pageable);
    Page<HealthRecord> findByUserIdAndRecordTypeAndIsDeletedFalseOrderByEventDateDesc(Long userId, String recordType, Pageable pageable);
    List<HealthRecord> findByUserIdAndAbnormalFlagGreaterThanAndIsDeletedFalseOrderByEventDateDesc(Long userId, Integer abnormalFlag);

    @Query("SELECT hr FROM HealthRecord hr WHERE hr.userId = :userId AND hr.eventDate BETWEEN :from AND :to AND hr.isDeleted = false ORDER BY hr.eventDate ASC")
    List<HealthRecord> findTimelineByDateRange(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT hr.source, COUNT(hr) FROM HealthRecord hr WHERE hr.createdAt >= :since AND hr.isDeleted = false GROUP BY hr.source")
    List<Object[]> countBySourceSince(LocalDateTime since);

    Optional<HealthRecord> findTopByUserIdAndRecordTypeInOrderByEventDateDesc(Long userId, List<String> recordTypes);
}
