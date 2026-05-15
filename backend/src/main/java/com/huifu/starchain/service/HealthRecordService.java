package com.huifu.starchain.service;

import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.HealthRecord;
import com.huifu.starchain.entity.LabReport;
import com.huifu.starchain.repository.HealthRecordRepository;
import com.huifu.starchain.repository.LabReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository recordRepo;
    private final LabReportRepository labRepo;

    public PageResult<HealthRecord> getTimeline(Long userId, int page, int size, String recordType) {
        var pg = (recordType == null || recordType.isBlank())
                ? recordRepo.findByUserIdAndIsDeletedFalseOrderByEventDateDesc(userId, PageRequest.of(page - 1, size))
                : recordRepo.findByUserIdAndRecordTypeAndIsDeletedFalseOrderByEventDateDesc(userId, recordType, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public List<HealthRecord> getTimelineByDateRange(Long userId, LocalDate from, LocalDate to) {
        return recordRepo.findTimelineByDateRange(userId, from, to);
    }

    public HealthRecord getRecord(Long id) {
        return recordRepo.findById(id).orElse(null);
    }

    public HealthRecord createRecord(HealthRecord record) {
        return recordRepo.save(record);
    }

    public List<HealthRecord> getAbnormalRecords(Long userId) {
        return recordRepo.findByUserIdAndAbnormalFlagGreaterThanAndIsDeletedFalseOrderByEventDateDesc(userId, 0);
    }

    // ---- Lab Reports ----
    public List<LabReport> getReportsByRecord(Long healthRecordId) {
        return labRepo.findByHealthRecordId(healthRecordId);
    }

    public List<LabReport> getTrend(String indicatorCode) {
        return labRepo.findTrendByIndicator(indicatorCode);
    }

    public Map<String, Long> getSourceStats(LocalDate since) {
        List<Object[]> rows = recordRepo.countBySourceSince(since.atStartOfDay());
        var map = new java.util.LinkedHashMap<String, Long>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }
}
