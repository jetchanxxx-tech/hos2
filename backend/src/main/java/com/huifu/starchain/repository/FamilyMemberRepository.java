package com.huifu.starchain.repository;

import com.huifu.starchain.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByFamilyId(Long familyId);
    List<FamilyMember> findByUserId(Long userId);
    Optional<FamilyMember> findByFamilyIdAndUserId(Long familyId, Long userId);
    void deleteByFamilyIdAndUserId(Long familyId, Long userId);
    long countByFamilyId(Long familyId);
}
