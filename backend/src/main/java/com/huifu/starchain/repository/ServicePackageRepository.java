package com.huifu.starchain.repository;

import com.huifu.starchain.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    Page<ServicePackage> findByStatusOrderBySortOrderAsc(ServicePackage.PkgStatus status, Pageable pageable);
    List<ServicePackage> findByCategoryAndStatus(String category, ServicePackage.PkgStatus status);
    List<ServicePackage> findByStatus(ServicePackage.PkgStatus status);

    @Query("SELECT sp.type, COUNT(sp) FROM ServicePackage sp WHERE sp.status = 'ON_SHELF' GROUP BY sp.type")
    List<Object[]> countByType();
}
