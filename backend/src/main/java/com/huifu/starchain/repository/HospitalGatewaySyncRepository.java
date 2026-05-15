package com.huifu.starchain.repository;

import com.huifu.starchain.entity.HospitalGatewaySync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalGatewaySyncRepository extends JpaRepository<HospitalGatewaySync, Long> {
    Optional<HospitalGatewaySync> findByDataTypeAndExternalId(String dataType, String externalId);
    List<HospitalGatewaySync> findBySyncStatusOrderByCreatedAtAsc(String syncStatus);
}
