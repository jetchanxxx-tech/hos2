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
class ApiSmokeTest {

    @Autowired private AuthService authService;
    @Autowired private ChatService chatService;
    @Autowired private DashboardService dashboardService;
    @Autowired private HealthRecordService healthRecordService;
    @Autowired private FollowupService followupService;
    @Autowired private UserRepository userRepo;
    @Autowired private UserService userService;
    @Autowired private ChatSessionRepository chatSessionRepo;

    private static Long userId, sessionId, familyId;

    @Test @Order(1)
    void test01_register() {
        var req = new com.huifu.starchain.dto.auth.RegisterRequest("13800000005", "test123", "测试用户", null, null);
        var res = authService.register(req);
        assertNotNull(res.accessToken());
        assertTrue(res.user().name().contains("*")); // 脱敏后包含 *
        userId = res.user().id();
    }

    @Test @Order(2)
    void test02_login() {
        var req = new com.huifu.starchain.dto.auth.LoginRequest("13800000005", "test123");
        var res = authService.login(req);
        assertNotNull(res.accessToken());
    }

    @Test @Order(3)
    void test03_concentricCircle() {
        var family = userService.createFamily(userId, "测试之家");
        familyId = family.getId();
        assertNotNull(familyId);
        assertEquals(1, family.getMemberCount());

        var viewed = userService.getFamilyByUserId(userId);
        assertNotNull(viewed);
        assertEquals("测试之家", viewed.getFamilyName());

        assertEquals(1, userService.getFamilyMembers(familyId).size());
    }

    @Test @Order(4)
    void test04_chatSession() {
        var session = chatService.startSession(userId, "MINIPROGRAM");
        assertNotNull(session.getId());
        sessionId = session.getId();
    }

    @Test @Order(5)
    void test05_sendNormalMessage() {
        var msg = chatService.sendMessage(sessionId, "USER", userId, "测试用户", "TEXT", "我有点腹痛");
        assertNotNull(msg);
        assertEquals(1, msg.getSeqNo());
        assertNotNull(msg.getContent());
    }

    @Test @Order(6)
    void test06_intentDetection() {
        // Check that intent type was set on the session
        var session = chatSessionRepo.findById(sessionId).orElseThrow();
        assertEquals("MEDICAL", session.getIntentType());
    }

    @Test @Order(7)
    void test07_sendEmergencyMessage() {
        var msg = chatService.sendMessage(sessionId, "USER", userId, "测试用户", "TEXT", "我出血了");
        assertNotNull(msg);
        assertTrue(Boolean.TRUE.equals(msg.getTriggerAlert()));
        assertEquals("出血", msg.getAlertKeyword());

        var session = chatSessionRepo.findById(sessionId).orElseThrow();
        assertEquals("URGENT", session.getEscalationLevel());
        assertEquals("WAITING_BUTLER", session.getStatus());
    }

    @Test @Order(8)
    void test08_aiReply() {
        var reply = chatService.sendAiReply(sessionId, "请立即就医，保持通话", "FAQ");
        assertNotNull(reply);
        assertEquals("AI", reply.getSenderType());
    }

    @Test @Order(9)
    void test09_getMessages() {
        var msgs = chatService.getMessages(sessionId);
        assertTrue(msgs.size() >= 3);
    }

    @Test @Order(10)
    void test10_getAlerts() {
        var alerts = chatService.getAlerts();
        assertFalse(alerts.isEmpty());
    }

    @Test @Order(11)
    void test11_dashboardKpi() {
        var kpis = dashboardService.getKpiSummary();
        assertNotNull(kpis);
        assertTrue(kpis.containsKey("activeMembers"));
    }

    @Test @Order(12)
    void test12_dashboardButlerLeaderboard() {
        var board = dashboardService.getButlerLeaderboard();
        assertNotNull(board);
        assertTrue(board.containsKey("topButlers"));
    }

    @Test @Order(13)
    void test13_healthRecord() {
        var record = new HealthRecord();
        record.setUserId(userId);
        record.setRecordType("CHECKUP");
        record.setEventTitle("首次产检");
        record.setEventDate(java.time.LocalDate.now());
        record.setSource("HIS");
        record.setGestationalWeek("7W");
        var saved = healthRecordService.createRecord(record);
        assertNotNull(saved.getId());

        var timeline = healthRecordService.getTimeline(userId, userId, 1, 20, null);
        assertTrue(timeline.getTotal() > 0);

        var fetched = healthRecordService.getRecord(saved.getId());
        assertEquals("首次产检", fetched.getEventTitle());
    }

    @Test @Order(14)
    void test14_followupStats() {
        var stats = followupService.getTaskStats();
        assertNotNull(stats);
    }
}
