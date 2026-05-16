package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.config.CryptoConfig.CryptoUtil;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class UserService {

    private final UserRepository userRepo;
    private final FamilyRepository familyRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final CryptoUtil cryptoUtil;

    public UserService(UserRepository userRepo, FamilyRepository familyRepo, FamilyMemberRepository familyMemberRepo, CryptoUtil cryptoUtil) { this.userRepo = userRepo; this.familyRepo = familyRepo; this.familyMemberRepo = familyMemberRepo; this.cryptoUtil = cryptoUtil; }

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(BizError.USER_NOT_FOUND));
    }

    @Transactional
    public User updateProfile(Long userId, User updated) {
        User user = getUserById(userId);
        if (updated.getBirthDate() != null) user.setBirthDate(updated.getBirthDate());
        if (updated.getGender() != null) user.setGender(updated.getGender());
        if (updated.getAvatarUrl() != null) user.setAvatarUrl(updated.getAvatarUrl());
        if (updated.getNameMasked() != null && !updated.getNameMasked().isBlank()) {
            user.setNameMasked(updated.getNameMasked());
            user.setRealName(cryptoUtil.encrypt(updated.getNameMasked()));
        }
        return userRepo.save(user);
    }

    @Transactional
    public FamilyMember joinByInviteCode(String inviteCode, Long userId) {
        Family family = familyRepo.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(40403, "邀请码无效，未找到对应家庭"));
        User user = getUserById(userId);
        if (user.getFamilyId() != null) {
            throw new BusinessException(409, "你已有所属家庭，请先退出后再加入新家庭");
        }
        return addFamilyMember(family.getId(), userId, "OTHER", "BASIC_ONLY");
    }

    public PageResult<User> listResidents(int page, int size) {
        var pg = userRepo.findByRole(User.UserRole.RESIDENT, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public PageResult<User> listButlers(int page, int size) {
        var pg = userRepo.findAll(
                (root, query, cb) -> root.get("role").in(User.UserRole.BUTLER_MEDICAL, User.UserRole.BUTLER_SERVICE),
                PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public PageResult<User> searchUsers(String keyword, int page, int size) {
        var pg = userRepo.searchByKeyword(keyword, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    // ---- Family ----
    public Family getFamilyByUserId(Long userId) {
        User user = getUserById(userId);
        if (user.getFamilyId() == null) return null;
        return familyRepo.findById(user.getFamilyId()).orElse(null);
    }

    public Family getFamily(Long familyId) {
        return familyRepo.findById(familyId)
                .orElseThrow(() -> new BusinessException(BizError.FAMILY_NOT_FOUND));
    }

    @Transactional
    public Family createFamily(Long userId, String familyName) {
        User user = getUserById(userId);
        if (user.getFamilyId() != null) {
            throw new BusinessException(409, "用户已有所属家庭");
        }
        Family family = new Family();
        family.setFamilyName(familyName);
        family.setPrimaryUserId(userId);
        family.setMemberCount(1);
        family.setInviteCode(generateInviteCode());
        family = familyRepo.save(family);
        user.setFamilyId(family.getId());
        userRepo.save(user);
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRelationship(FamilyMember.Relationship.SELF);
        member.setShareScope("ALL");
        familyMemberRepo.save(member);
        return family;
    }

    @Transactional
    public FamilyMember addFamilyMember(Long familyId, Long memberUserId, String relationship, String shareScope) {
        Family family = getFamily(familyId);
        User member = getUserById(memberUserId);
        if (member.getFamilyId() != null && !member.getFamilyId().equals(familyId)) {
            throw new BusinessException(409, "该用户已属于其他家庭");
        }
        if (familyMemberRepo.findByFamilyIdAndUserId(familyId, memberUserId).isPresent()) {
            throw new BusinessException(409, "该成员已在家庭中");
        }
        FamilyMember fm = new FamilyMember();
        fm.setFamilyId(familyId);
        fm.setUserId(memberUserId);
        fm.setRelationship(FamilyMember.Relationship.valueOf(relationship));
        fm.setShareScope(shareScope);
        fm = familyMemberRepo.save(fm);
        member.setFamilyId(familyId);
        userRepo.save(member);
        family.setMemberCount((int) familyMemberRepo.countByFamilyId(familyId));
        familyRepo.save(family);
        return fm;
    }

    @Transactional
    public void removeFamilyMember(Long familyId, Long memberUserId) {
        familyMemberRepo.deleteByFamilyIdAndUserId(familyId, memberUserId);
        User member = getUserById(memberUserId);
        member.setFamilyId(null);
        userRepo.save(member);
        Family family = getFamily(familyId);
        family.setMemberCount((int) familyMemberRepo.countByFamilyId(familyId));
        familyRepo.save(family);
    }

    @Transactional
    public void dissolveFamily(Long familyId, Long requesterUserId) {
        Family family = getFamily(familyId);
        if (!family.getPrimaryUserId().equals(requesterUserId)) {
            throw new BusinessException(BizError.FORBIDDEN);
        }
        long memberCount = familyMemberRepo.countByFamilyId(familyId);
        if (memberCount > 1) {
            throw new BusinessException(409, "请先移除所有成员后再解散家庭");
        }
        // 清除最后一名成员(主账号)的关联
        familyMemberRepo.deleteByFamilyIdAndUserId(familyId, requesterUserId);
        User user = getUserById(requesterUserId);
        user.setFamilyId(null);
        userRepo.save(user);
        family.setStatus(Family.FamilyStatus.DISSOLVED);
        family.setMemberCount(0);
        familyRepo.save(family);
    }

    @Transactional
    public FamilyMember updateMemberShareScope(Long familyId, Long memberUserId, String shareScope, Long requesterUserId) {
        Family family = getFamily(familyId);
        if (!family.getPrimaryUserId().equals(requesterUserId)) {
            throw new BusinessException(BizError.FORBIDDEN);
        }
        FamilyMember fm = familyMemberRepo.findByFamilyIdAndUserId(familyId, memberUserId)
                .orElseThrow(() -> new BusinessException(BizError.NOT_FOUND));
        fm.setShareScope(shareScope);
        return familyMemberRepo.save(fm);
    }

    @Transactional
    public FamilyMember toggleEmergencyContact(Long familyId, Long memberUserId, Long requesterUserId) {
        Family family = getFamily(familyId);
        if (!family.getPrimaryUserId().equals(requesterUserId)) {
            throw new BusinessException(BizError.FORBIDDEN);
        }
        FamilyMember fm = familyMemberRepo.findByFamilyIdAndUserId(familyId, memberUserId)
                .orElseThrow(() -> new BusinessException(BizError.NOT_FOUND));
        fm.setIsEmergencyContact(!Boolean.TRUE.equals(fm.getIsEmergencyContact()));
        return familyMemberRepo.save(fm);
    }

    public List<FamilyMember> getFamilyMembers(Long familyId) {
        return familyMemberRepo.findByFamilyId(familyId);
    }

    private String generateInviteCode() {
        return "HF" + String.format("%08d", System.currentTimeMillis() % 100000000);
    }
}
