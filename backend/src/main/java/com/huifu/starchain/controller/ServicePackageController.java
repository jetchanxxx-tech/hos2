package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.PackageOrder;
import com.huifu.starchain.entity.ServicePackage;
import com.huifu.starchain.service.ServicePackageService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/packages")

public class ServicePackageController {

    private final ServicePackageService pkgService;

    @GetMapping
    public ApiResponse<PageResult<ServicePackage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(pkgService.listPackages(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ServicePackage> detail(@PathVariable Long id) {
        return ApiResponse.ok(pkgService.getPackage(id));
    }

    @PostMapping("/{id}/order")
    public ApiResponse<PackageOrder> order(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResponse.ok(pkgService.createOrder(userId, id));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<PackageOrder>> myOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(pkgService.getMyOrders(userId, page, size));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<PackageOrder> getOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(pkgService.getOrder(orderId));
    }

    @PostMapping("/orders/{orderId}/redeem")
    public ApiResponse<?> redeem(@PathVariable Long orderId,
                                  @RequestParam Long userId,
                                  @RequestParam String benefitType,
                                  @RequestParam Long butlerId) {
        return ApiResponse.ok(pkgService.redeemBenefit(orderId, userId, benefitType, butlerId));
    }
}
