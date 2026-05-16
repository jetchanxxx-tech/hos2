package com.huifu.starchain.service;

import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.Complaint;
import com.huifu.starchain.repository.ComplaintRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepo;

    public ComplaintService(ComplaintRepository complaintRepo) { this.complaintRepo = complaintRepo; }

    public PageResult<Complaint> listByStatus(String status, int page, int size) {
        var pg = complaintRepo.findByStatusOrderByCreatedAtDesc(status != null ? status : "PENDING", PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    @Transactional
    public Complaint create(Complaint complaint) {
        complaint.setStatus("PENDING");
        return complaintRepo.save(complaint);
    }

    @Transactional
    public Complaint assign(Long id, Long butlerId) {
        var c = complaintRepo.findById(id).orElse(null);
        if (c == null) return null;
        c.setAssignedTo(butlerId);
        c.setStatus("PROCESSING");
        return complaintRepo.save(c);
    }

    @Transactional
    public Complaint resolve(Long id, String resolution, Long resolvedBy) {
        var c = complaintRepo.findById(id).orElse(null);
        if (c == null) return null;
        c.setResolution(resolution);
        c.setResolvedBy(resolvedBy);
        c.setResolvedAt(LocalDateTime.now());
        c.setStatus("RESOLVED");
        return complaintRepo.save(c);
    }

    @Transactional
    public Complaint close(Long id) {
        var c = complaintRepo.findById(id).orElse(null);
        if (c == null) return null;
        c.setClosedAt(LocalDateTime.now());
        c.setStatus("CLOSED");
        return complaintRepo.save(c);
    }
}
