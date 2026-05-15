package com.huifu.starchain.repository;

import com.huifu.starchain.entity.BenefitRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BenefitRedemptionRepository extends JpaRepository<BenefitRedemption, Long> {
    List<BenefitRedemption> findByOrderId(Long orderId);
    List<BenefitRedemption> findByUserId(Long userId);
    List<BenefitRedemption> findByUserIdAndBenefitType(Long userId, String benefitType);
    long countByRedeemedByAndRedeemedAtBetween(Long redeemedBy, LocalDateTime from, LocalDateTime to);
}
