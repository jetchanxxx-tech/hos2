package com.huifu.starchain.repository;

import com.huifu.starchain.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Complaint> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<Complaint> findByAssignedToOrderByCreatedAtDesc(Long assignedTo, Pageable pageable);

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    java.util.List<Object[]> countByStatus();
}
