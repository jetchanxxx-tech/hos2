package com.huifu.starchain;

import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;
import com.huifu.starchain.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConcentricCircleTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepo;
    @Autowired private FamilyRepository familyRepo;
    @Autowired private FamilyMemberRepository familyMemberRepo;

    private Long userId1, userId2, familyId;

    @Test @Order(1)
    void test01_createUsersAndFamily() {
        // Setup users
        User u1 = newUser("13800000001", "T*t1", "hash1");
        User u2 = newUser("13800000002", "T*t2", "hash2");
        userId1 = u1.getId();
        userId2 = u2.getId();
        assertNotNull(userId1);

        // Create family
        Family f = userService.createFamily(userId1, "测试家庭");
        familyId = f.getId();
        assertNotNull(familyId);
        assertEquals("测试家庭", f.getFamilyName());
        assertEquals(1, f.getMemberCount());
        assertTrue(f.getInviteCode().startsWith("HF"));
    }

    @Test @Order(2)
    void test02_cannotCreateDuplicateFamily() {
        try {
            userService.createFamily(userId1, "另一个");
            fail("Should throw");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("家庭"));
        }
    }

    @Test @Order(3)
    void test03_addFamilyMember() {
        var fm = userService.addFamilyMember(familyId, userId2, "SPOUSE", "ALL");
        assertNotNull(fm.getId());
        assertEquals(FamilyMember.Relationship.SPOUSE, fm.getRelationship());

        var f = familyRepo.findById(familyId).orElseThrow();
        assertEquals(2, f.getMemberCount());
    }

    @Test @Order(4)
    void test04_getMembers() {
        var members = userService.getFamilyMembers(familyId);
        assertEquals(2, members.size());
    }

    @Test @Order(5)
    void test05_getFamilyByUserId() {
        var f = userService.getFamilyByUserId(userId1);
        assertNotNull(f);
        assertEquals("测试家庭", f.getFamilyName());
    }

    @Test @Order(6)
    void test06_updateShareScope() {
        var fm = userService.updateMemberShareScope(familyId, userId2, "REPORT_ONLY", userId1);
        assertEquals("REPORT_ONLY", fm.getShareScope());
    }

    @Test @Order(7)
    void test07_toggleEmergency() {
        var fm = userService.toggleEmergencyContact(familyId, userId2, userId1);
        assertTrue(fm.getIsEmergencyContact());
        fm = userService.toggleEmergencyContact(familyId, userId2, userId1);
        assertFalse(fm.getIsEmergencyContact());
    }

    @Test @Order(8)
    void test08_inviteCode() {
        var f = familyRepo.findById(familyId).orElseThrow();
        var found = familyRepo.findByInviteCode(f.getInviteCode());
        assertTrue(found.isPresent());
    }

    @Test @Order(9)
    void test09_dissolveFailsWithMultiple() {
        try {
            userService.dissolveFamily(familyId, userId1);
            fail("Should throw");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("成员"));
        }
    }

    @Test @Order(10)
    void test10_removeMember() {
        userService.removeFamilyMember(familyId, userId2);
        assertEquals(1, userService.getFamilyMembers(familyId).size());
        assertNull(userRepo.findById(userId2).orElseThrow().getFamilyId());
    }

    @Test @Order(11)
    void test11_dissolveFamily() {
        userService.dissolveFamily(familyId, userId1);
        assertEquals(Family.FamilyStatus.DISSOLVED, familyRepo.findById(familyId).orElseThrow().getStatus());
        assertNull(userRepo.findById(userId1).orElseThrow().getFamilyId());
    }

    @Test @Order(12)
    void test12_searchUsers() {
        var result = userService.searchUsers("T", 1, 20);
        assertTrue(result.getTotal() > 0);
    }

    private User newUser(String phone, String masked, String hash) {
        User u = new User();
        u.setPhone("enc_" + phone);
        u.setPhoneHash(hash);
        u.setNameMasked(masked);
        u.setRealName("enc_Name");
        u.setRole(User.UserRole.RESIDENT);
        u.setStatus(User.UserStatus.ACTIVE);
        u.setDataAuthConsent(1);
        return userRepo.save(u);
    }
}
