package com.huifu.starchain.repository;

import com.huifu.starchain.entity.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByHealthRecordId(Long healthRecordId);
    List<LabReport> findByIndicatorCodeAndIsAbnormalNotOrderByReportDateDesc(String indicatorCode, Integer isAbnormal);
    List<LabReport> findByIsAbnormalAndReportDateAfter(Integer isAbnormal, LocalDate reportDate);

    @Query("SELECT lr FROM LabReport lr WHERE lr.indicatorCode = :code ORDER BY lr.reportDate ASC")
    List<LabReport> findTrendByIndicator(String code);
}
