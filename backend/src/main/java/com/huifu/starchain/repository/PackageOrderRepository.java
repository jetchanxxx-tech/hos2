package com.huifu.starchain.repository;

import com.huifu.starchain.entity.PackageOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageOrderRepository extends JpaRepository<PackageOrder, Long> {
    Optional<PackageOrder> findByOrderNo(String orderNo);
    Page<PackageOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<PackageOrder> findByStatus(String status, Pageable pageable);
    List<PackageOrder> findByUserIdAndStatusAndEndDateAfter(Long userId, String status, LocalDate date);

    @Query("SELECT COALESCE(SUM(po.amount), 0) FROM PackageOrder po WHERE po.status = 'PAID' AND po.paidAt >= :since")
    BigDecimal sumRevenueSince(LocalDateTime since);

    @Query("SELECT po.packageId, COUNT(po) as cnt, SUM(po.amount) as revenue FROM PackageOrder po WHERE po.status IN ('PAID', 'ACTIVE') AND po.paidAt >= :since GROUP BY po.packageId ORDER BY cnt DESC")
    List<Object[]> salesRankingSince(LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT po.userId) FROM PackageOrder po WHERE po.status = 'PAID'")
    long countDistinctUserId();

    @Query("SELECT COALESCE(SUM(po.amount), 0) FROM PackageOrder po WHERE po.status = 'PAID' AND po.paidAt >= :from AND po.paidAt < :to")
    BigDecimal sumRevenueBetween(LocalDateTime from, LocalDateTime to);
}
