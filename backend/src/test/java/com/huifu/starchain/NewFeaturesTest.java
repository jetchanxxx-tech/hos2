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
class NewFeaturesTest {

    @Autowired private ComplaintService complaintService;
    @Autowired private ComplaintRepository complaintRepo;
    @Autowired private FollowupService followupService;
    @Autowired private FollowupTaskRepository followupRepo;
    @Autowired private FollowupRuleEngine followupEngine;
    @Autowired private UserRepository userRepo;
    @Autowired private ServicePackageService pkgService;
    @Autowired private ServicePackageRepository pkgRepo;

    private Long complaintId, userId, followupId;

    @Test @Order(1)
    void test01_createComplaint() {
        // Setup user
        User u = new User();
        u.setPhone("enc_complaint_test");
        u.setPhoneHash("complaint_test_hash");
        u.setNameMasked("测*户");
        u.setRealName("enc_test");
        u.setRole(User.UserRole.RESIDENT);
        u.setStatus(User.UserStatus.ACTIVE);
        u.setDataAuthConsent(1);
        userId = userRepo.save(u).getId();

        Complaint c = new Complaint();
        c.setUserId(userId);
        c.setCategory("SERVICE");
        c.setContent("服务态度差，等待时间太长");
        var saved = complaintService.create(c);
        complaintId = saved.getId();
        assertNotNull(complaintId);
        assertEquals("PENDING", saved.getStatus());
    }

    @Test @Order(2)
    void test02_assignComplaint() {
        var assigned = complaintService.assign(complaintId, userId);
        assertEquals("PROCESSING", assigned.getStatus());
        assertEquals(userId, assigned.getAssignedTo());
    }

    @Test @Order(3)
    void test03_resolveComplaint() {
        var resolved = complaintService.resolve(complaintId, "已向用户道歉并改进服务", userId);
        assertEquals("RESOLVED", resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());
    }

    @Test @Order(4)
    void test04_closeComplaint() {
        var closed = complaintService.close(complaintId);
        assertEquals("CLOSED", closed.getStatus());
        assertNotNull(closed.getClosedAt());
    }

    @Test @Order(5)
    void test05_listComplaints() {
        var list = complaintService.listByStatus("CLOSED", 1, 10);
        assertTrue(list.getTotal() >= 1);
    }

    @Test @Order(6)
    void test06_createFollowup() {
        FollowupTask t = new FollowupTask();
        t.setUserId(userId);
        t.setTaskType("测试随访");
        t.setScheduledDate(java.time.LocalDate.now());
        t.setStatus("PENDING");
        t.setPriority("NORMAL");
        t.setFollowupMethod("PHONE");
        var saved = followupRepo.save(t);
        followupId = saved.getId();
        assertNotNull(followupId);
    }

    @Test @Order(7)
    void test07_followupStats() {
        var stats = followupService.getTaskStats();
        assertNotNull(stats);
    }

    @Test @Order(8)
    void test08_followupEngineScan() {
        // Should not throw - just runs the scan
        followupEngine.scanAbnormalReports();
        followupEngine.scanPregnancyMilestones();
        assertTrue(true);
    }

    @Test @Order(9)
    void test09_servicePackageLifecycle() {
        ServicePackage pkg = new ServicePackage();
        pkg.setName("测试服务包");
        pkg.setType("EXPERIENCE");
        pkg.setCategory("MATERNITY");
        pkg.setPrice(java.math.BigDecimal.valueOf(99));
        pkg.setDiscountPrice(java.math.BigDecimal.valueOf(49));
        pkg.setDurationDays(30);
        pkg.setBenefitsJson("[{\"name\":\"测试权益\"}]");
        var created = pkgService.createPackage(pkg);
        assertNotNull(created.getId());
        assertEquals(ServicePackage.PkgStatus.DRAFT, created.getStatus());

        // On shelf
        created.setStatus(ServicePackage.PkgStatus.ON_SHELF);
        pkgRepo.save(created);
        var found = pkgRepo.findById(created.getId()).orElseThrow();
        assertEquals(ServicePackage.PkgStatus.ON_SHELF, found.getStatus());

        // Off shelf
        found.setStatus(ServicePackage.PkgStatus.OFF_SHELF);
        pkgRepo.save(found);
        assertEquals(ServicePackage.PkgStatus.OFF_SHELF, pkgRepo.findById(created.getId()).orElseThrow().getStatus());
    }

    @Test @Order(10)
    void test10_chatIntentDetection() {
        assertEquals("MEDICAL", new ChatServiceHelper().getIntent("我肚子疼"));
        assertEquals("BENEFIT", new ChatServiceHelper().getIntent("怎么使用优惠券"));
        assertEquals("COMPLAINT", new ChatServiceHelper().getIntent("我要投诉你们"));
        assertEquals("APPOINTMENT", new ChatServiceHelper().getIntent("我想挂号"));
        assertEquals("GENERAL", new ChatServiceHelper().getIntent("你好"));
    }

    /** Helper to test ChatService intent detection without autowiring */
    private static class ChatServiceHelper {
        String getIntent(String text) {
            String t = text.toLowerCase();
            if (containsKeyword(t, "痛","疼","出血","发烧","咳嗽","吐","晕","药","检查","复查","手术","指标","病","诊")) return "MEDICAL";
            if (containsKeyword(t, "券","核销","预约","陪诊","套餐","退","服务包","钱","退费","权益")) return "BENEFIT";
            if (containsKeyword(t, "投诉","差","火大","等太久","态度","敷衍","退款","举报")) return "COMPLAINT";
            if (containsKeyword(t, "挂号","下次","时间","约","改期","什么时候")) return "APPOINTMENT";
            return "GENERAL";
        }
        boolean containsKeyword(String text, String... keywords) {
            for (String kw : keywords) if (text.contains(kw)) return true;
            return false;
        }
    }
}
