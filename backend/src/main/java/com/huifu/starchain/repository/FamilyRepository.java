package com.huifu.starchain.repository;

import com.huifu.starchain.entity.Family;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByInviteCode(String inviteCode);
    Optional<Family> findByPrimaryUserId(Long primaryUserId);

    @Query("SELECT f FROM Family f WHERE f.status = 'ACTIVE'")
    Page<Family> findAllActive(Pageable pageable);
}
