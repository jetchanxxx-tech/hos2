package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.service.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/me")
    public ApiResponse<User> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.getUserById(userId));
    }

    @GetMapping
    public ApiResponse<PageResult<User>> listResidents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(userService.listResidents(page, size));
    }

    @GetMapping("/butlers")
    public ApiResponse<PageResult<User>> listButlers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(userService.listButlers(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserById(id));
    }
}

@RestController
@RequestMapping("/api/v1/families")

class FamilyController {

    private final UserService userService;

    FamilyController(UserService userService) { this.userService = userService; }

    @GetMapping("/me")
    public ApiResponse<Family> myFamily(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.getFamilyByUserId(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Family> getFamily(@PathVariable Long id) {
        return ApiResponse.ok(userService.getFamily(id));
    }

    @PostMapping
    public ApiResponse<Family> createFamily(@AuthenticationPrincipal Long userId,
                                             @RequestParam String familyName) {
        return ApiResponse.ok(userService.createFamily(userId, familyName));
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<FamilyMember>> getMembers(@PathVariable Long id) {
        return ApiResponse.ok(userService.getFamilyMembers(id));
    }

    @PostMapping("/{id}/members")
    public ApiResponse<FamilyMember> addMember(@PathVariable Long id,
                                                @RequestParam Long userId,
                                                @RequestParam(defaultValue = "OTHER") String relationship,
                                                @RequestParam(defaultValue = "ALL") String shareScope) {
        return ApiResponse.ok(userService.addFamilyMember(id, userId, relationship, shareScope));
    }

    @DeleteMapping("/{id}/members/{memberUserId}")
    public ApiResponse<Void> removeMember(@PathVariable Long id, @PathVariable Long memberUserId) {
        userService.removeFamilyMember(id, memberUserId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> dissolveFamily(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        userService.dissolveFamily(id, userId);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/members/{memberUserId}/share-scope")
    public ApiResponse<FamilyMember> updateShareScope(
            @PathVariable Long id,
            @PathVariable Long memberUserId,
            @RequestParam String shareScope,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.updateMemberShareScope(id, memberUserId, shareScope, userId));
    }

    @PutMapping("/{id}/members/{memberUserId}/emergency")
    public ApiResponse<FamilyMember> toggleEmergency(
            @PathVariable Long id,
            @PathVariable Long memberUserId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userService.toggleEmergencyContact(id, memberUserId, userId));
    }
}
