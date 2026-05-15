package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service

public class ServicePackageService {

    private final ServicePackageRepository pkgRepo;
    private final PackageOrderRepository orderRepo;
    private final BenefitRedemptionRepository redemptionRepo;

    public ServicePackageService(ServicePackageRepository pkgRepo, PackageOrderRepository orderRepo, BenefitRedemptionRepository redemptionRepo) { this.pkgRepo = pkgRepo; this.orderRepo = orderRepo; this.redemptionRepo = redemptionRepo; }

    // ---- Service Packages ----
    public PageResult<ServicePackage> listPackages(int page, int size) {
        var pg = pkgRepo.findByStatusOrderBySortOrderAsc(ServicePackage.PkgStatus.ON_SHELF, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public ServicePackage getPackage(Long id) {
        return pkgRepo.findById(id)
                .orElseThrow(() -> new BusinessException(BizError.PACKAGE_NOT_FOUND));
    }

    public ServicePackage createPackage(ServicePackage pkg) {
        return pkgRepo.save(pkg);
    }

    public ServicePackage updatePackage(Long id, ServicePackage update) {
        ServicePackage pkg = getPackage(id);
        if (update.getName() != null) pkg.setName(update.getName());
        if (update.getPrice() != null) pkg.setPrice(update.getPrice());
        if (update.getDiscountPrice() != null) pkg.setDiscountPrice(update.getDiscountPrice());
        if (update.getBenefitsJson() != null) pkg.setBenefitsJson(update.getBenefitsJson());
        if (update.getStatus() != null) pkg.setStatus(update.getStatus());
        return pkgRepo.save(pkg);
    }

    // ---- Orders ----
    @Transactional
    public PackageOrder createOrder(Long userId, Long packageId) {
        ServicePackage pkg = getPackage(packageId);
        if (pkg.getStatus() != ServicePackage.PkgStatus.ON_SHELF) {
            throw new BusinessException(BizError.PACKAGE_OFF_SHELF);
        }
        BigDecimal actualAmount = pkg.getDiscountPrice() != null ? pkg.getDiscountPrice() : pkg.getPrice();
        String orderNo = "HF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", System.currentTimeMillis() % 10000);
        PackageOrder order = new PackageOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setAmount(actualAmount);
        order.setOriginalAmount(pkg.getPrice());
        order.setStatus("PAID");
        order.setStartDate(LocalDate.now());
        order.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentMethod("WECHAT_PAY");
        return orderRepo.save(order);
    }

    public PageResult<PackageOrder> getMyOrders(Long userId, int page, int size) {
        var pg = orderRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public PackageOrder getOrder(Long orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(BizError.ORDER_NOT_FOUND));
    }

    // ---- Redemptions ----
    @Transactional
    public BenefitRedemption redeemBenefit(Long orderId, Long userId, String benefitType, Long butlerId) {
        PackageOrder order = getOrder(orderId);
        if (order.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessException(BizError.ORDER_EXPIRED);
        }
        BenefitRedemption redemption = redemptionRepo.findByUserIdAndBenefitType(userId, benefitType)
                .stream().findFirst().orElse(null);
        if (redemption == null) {
            redemption = new BenefitRedemption();
            redemption.setOrderId(orderId);
            redemption.setPackageId(order.getPackageId());
            redemption.setUserId(userId);
            redemption.setBenefitType(benefitType);
            redemption.setBenefitName(benefitType);
            redemption.setTotalCount(1);
            redemption.setUsedCount(1);
            redemption.setStatus("PARTIALLY_USED");
            redemption.setRedeemedBy(butlerId);
            redemption.setRedeemedAt(LocalDateTime.now());
        } else {
            if (redemption.getUsedCount() >= redemption.getTotalCount()) {
                throw new BusinessException(BizError.BENEFIT_EXHAUSTED);
            }
            redemption.setUsedCount(redemption.getUsedCount() + 1);
            redemption.setRedeemedBy(butlerId);
            redemption.setRedeemedAt(LocalDateTime.now());
            if (redemption.getUsedCount() >= redemption.getTotalCount()) {
                redemption.setStatus("EXHAUSTED");
            }
        }
        return redemptionRepo.save(redemption);
    }
}
