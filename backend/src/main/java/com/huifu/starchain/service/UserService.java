package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.config.CryptoConfig.CryptoUtil;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final FamilyRepository familyRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final CryptoUtil cryptoUtil;

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new BusinessException(BizError.USER_NOT_FOUND));
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
        Family family = Family.builder()
                .familyName(familyName)
                .primaryUserId(userId)
                .memberCount(1)
                .inviteCode(generateInviteCode())
                .build();
        family = familyRepo.save(family);
        user.setFamilyId(family.getId());
        userRepo.save(user);
        FamilyMember member = FamilyMember.builder()
                .familyId(family.getId())
                .userId(userId)
                .relationship(FamilyMember.Relationship.SELF)
                .shareScope("ALL")
                .build();
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
        FamilyMember fm = FamilyMember.builder()
                .familyId(familyId)
                .userId(memberUserId)
                .relationship(FamilyMember.Relationship.valueOf(relationship))
                .shareScope(shareScope)
                .build();
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

    public List<FamilyMember> getFamilyMembers(Long familyId) {
        return familyMemberRepo.findByFamilyId(familyId);
    }

    private String generateInviteCode() {
        return "HF" + String.format("%08d", System.currentTimeMillis() % 100000000);
    }
}
