package com.huifu.starchain.repository;

import com.huifu.starchain.entity.FollowupTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FollowupTaskRepository extends JpaRepository<FollowupTask, Long> {
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

    @Query("SELECT ft.assignedButlerId, COUNT(ft) FROM FollowupTask ft WHERE ft.assignedButlerId IS NOT NULL AND ft.status = 'COMPLETED' GROUP BY ft.assignedButlerId ORDER BY COUNT(ft) DESC")
    List<Object[]> butlerCompletionRanking();
}
