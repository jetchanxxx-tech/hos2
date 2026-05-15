package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class HealthRecordService {

    private final HealthRecordRepository recordRepo;
    private final LabReportRepository labRepo;
    private final FamilyMemberRepository familyMemberRepo;

    public HealthRecordService(HealthRecordRepository recordRepo, LabReportRepository labRepo, FamilyMemberRepository familyMemberRepo) { this.recordRepo = recordRepo; this.labRepo = labRepo; this.familyMemberRepo = familyMemberRepo; }

    /**
     * 检查 requester 是否有权查看 targetUserId 的健康数据
     * @return 共享范围：ALL / REPORT_ONLY / BASIC_ONLY，null=无权
     */
    private String resolveShareScope(Long requesterUserId, Long targetUserId) {
        if (requesterUserId.equals(targetUserId)) return "ALL"; // 本人
        List<FamilyMember> requesterMemberships = familyMemberRepo.findByUserId(requesterUserId);
        for (FamilyMember fm : requesterMemberships) {
            if (familyMemberRepo.findByFamilyIdAndUserId(fm.getFamilyId(), targetUserId).isPresent()) {
                return fm.getShareScope(); // 在同一家庭中
            }
        }
        return null; // 不在任何共同家庭中
    }

    public PageResult<HealthRecord> getTimeline(Long requesterUserId, Long targetUserId, int page, int size, String recordType) {
        String scope = resolveShareScope(requesterUserId, targetUserId);
        if (scope == null) throw new BusinessException(BizError.FORBIDDEN);
        var pg = (recordType == null || recordType.isBlank())
                ? recordRepo.findByUserIdAndIsDeletedFalseOrderByEventDateDesc(targetUserId, PageRequest.of(page - 1, size))
                : recordRepo.findByUserIdAndRecordTypeAndIsDeletedFalseOrderByEventDateDesc(targetUserId, recordType, PageRequest.of(page - 1, size));
        List<HealthRecord> content = maskByScope(pg.getContent(), scope);
        return PageResult.of(content, pg.getTotalElements(), page, size);
    }

    public List<HealthRecord> getTimelineByDateRange(Long requesterUserId, Long targetUserId, LocalDate from, LocalDate to) {
        String scope = resolveShareScope(requesterUserId, targetUserId);
        if (scope == null) throw new BusinessException(BizError.FORBIDDEN);
        return maskByScope(recordRepo.findTimelineByDateRange(targetUserId, from, to), scope);
    }

    public List<HealthRecord> getAbnormalRecords(Long requesterUserId, Long targetUserId) {
        String scope = resolveShareScope(requesterUserId, targetUserId);
        if (scope == null) throw new BusinessException(BizError.FORBIDDEN);
        return maskByScope(recordRepo.findByUserIdAndAbnormalFlagGreaterThanAndIsDeletedFalseOrderByEventDateDesc(targetUserId, 0), scope);
    }

    public HealthRecord getRecord(Long id) {
        return recordRepo.findById(id).orElse(null);
    }

    public HealthRecord createRecord(HealthRecord record) {
        return recordRepo.save(record);
    }

    // ---- Lab Reports ----
    /** 仅 ALL 和 REPORT_ONLY 可查看报告详情，BASIC_ONLY/NONE 拦截 */
    public List<LabReport> getReportsByRecord(Long requesterUserId, Long healthRecordId) {
        HealthRecord record = recordRepo.findById(healthRecordId).orElse(null);
        if (record == null) return List.of();
        String scope = resolveShareScope(requesterUserId, record.getUserId());
        if (scope == null || "BASIC_ONLY".equals(scope) || "NONE".equals(scope)) {
            throw new BusinessException(BizError.FORBIDDEN);
        }
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

    /** 根据共享范围脱敏：BASIC_ONLY 仅保留基础信息 */
    private List<HealthRecord> maskByScope(List<HealthRecord> records, String scope) {
        if ("ALL".equals(scope) || "REPORT_ONLY".equals(scope)) return records;
        if ("BASIC_ONLY".equals(scope)) {
            return records.stream().map(r -> {
                HealthRecord masked = new HealthRecord();
                masked.setId(r.getId());
                masked.setUserId(r.getUserId());
                masked.setEventDate(r.getEventDate());
                masked.setRecordType(r.getRecordType());
                masked.setEventTitle(r.getEventTitle());
                // 不返回 eventSummary/detailJson/abnormalFlag 等敏感字段
                return masked;
            }).collect(Collectors.toList());
        }
        return List.of(); // NONE
    }
}
