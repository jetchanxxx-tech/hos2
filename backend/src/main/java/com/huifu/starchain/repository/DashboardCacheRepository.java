package com.huifu.starchain.repository;

import com.huifu.starchain.entity.DashboardCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DashboardCacheRepository extends JpaRepository<DashboardCache, Long> {
    List<DashboardCache> findByMetricNameAndDimensionAndPeriodStartBetweenOrderByCalculatedAtAsc(
            String metricName, String dimension, LocalDate from, LocalDate to);
    List<DashboardCache> findByDimensionAndPeriodStart(String dimension, LocalDate periodStart);
}
