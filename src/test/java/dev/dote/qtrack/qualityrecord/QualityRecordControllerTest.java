package dev.dote.qtrack.qualityrecord;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.dote.qtrack._core.security.JwtUtil;
import dev.dote.qtrack.dailyproduction.DailyProduction;
import dev.dote.qtrack.dailyproduction.DailyProductionRepository;
import dev.dote.qtrack.item.Item;
import dev.dote.qtrack.item.ItemRepository;
import dev.dote.qtrack.process.Process;
import dev.dote.qtrack.process.ProcessRepository;
import dev.dote.qtrack.systemcode.SystemCode;
import dev.dote.qtrack.systemcode.SystemCodeRepository;
import dev.dote.qtrack.user.Role;
import dev.dote.qtrack.user.User;
import dev.dote.qtrack.user.UserRepository;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class QualityRecordControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    private ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private QualityRecordRepository qualityRecordRepository;

    @Autowired
    private DailyProductionRepository dailyProductionRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SystemCodeRepository systemCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String managerToken;
    private Item testItem;
    private Process testProcess;
    private DailyProduction testDailyProduction;

    @BeforeEach
    void setUp() {
        qualityRecordRepository.deleteAll();
        dailyProductionRepository.deleteAll();
        processRepository.deleteAll();
        itemRepository.deleteAll();
        systemCodeRepository.deleteAll();
        userRepository.deleteAll();
        
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 테스트용 사용자 생성 및 토큰 생성
        User user = new User("testuser", passwordEncoder.encode("password123"), Role.USER);
        User manager = new User("testmanager", passwordEncoder.encode("password123"), Role.MANAGER);
        userRepository.save(user);
        userRepository.save(manager);

        userToken = jwtUtil.generateToken(user.getId(), user.getRole());
        managerToken = jwtUtil.generateToken(manager.getId(), manager.getRole());

        // 테스트용 부품, 공정, 일별 생산 데이터 생성
        testItem = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(testItem);

        testProcess = new Process("W", "작업", "작업 공정", 1);
        processRepository.save(testProcess);

        testDailyProduction = new DailyProduction(testItem, LocalDate.of(2025, 1, 15), 1000);
        dailyProductionRepository.save(testDailyProduction);

        // 테스트용 시스템 코드 생성
        SystemCode code1 = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD", "0.5", "NG 비율 임계값", true);
        SystemCode code2 = new SystemCode("EVALUATION", "INCREASE_RATE_THRESHOLD", "2.0", "증가율 임계값", true);
        systemCodeRepository.save(code1);
        systemCodeRepository.save(code2);
    }

    @Test
    void findAll_test() throws Exception {
        // given
        QualityRecord qr1 = new QualityRecord(testDailyProduction, testProcess, 900, 100);
        qualityRecordRepository.save(qr1);

        // when
        ResultActions result = mvc.perform(
                get("/api/quality-records")
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(1));
    }

    @Test
    void findById_test() throws Exception {
        // given
        QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
        qualityRecordRepository.save(qr);
        Long qrId = qr.getId();

        // when
        ResultActions result = mvc.perform(
                get("/api/quality-records/" + qrId)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.id").value(qrId.intValue()))
                .andExpect(jsonPath("$.body.okQuantity").value(900))
                .andExpect(jsonPath("$.body.ngQuantity").value(100))
                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                .andExpect(jsonPath("$.body.ngRate").value(10.0));
    }

    @Test
    void create_as_user_test() throws Exception {
        // given
        QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                testDailyProduction.getId(),
                testProcess.getId(),
                900,
                100
        );
        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions result = mvc.perform(
                post("/api/quality-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.id").exists())
                .andExpect(jsonPath("$.body.okQuantity").value(900))
                .andExpect(jsonPath("$.body.ngQuantity").value(100))
                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                .andExpect(jsonPath("$.body.ngRate").value(10.0));
    }

    @Test
    void create_duplicate_daily_production_process_test() throws Exception {
        // given
        QualityRecord existing = new QualityRecord(testDailyProduction, testProcess, 800, 200);
        qualityRecordRepository.save(existing);

        QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                testDailyProduction.getId(),
                testProcess.getId(),
                900,
                100
        );
        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions result = mvc.perform(
                post("/api/quality-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.msg", containsString("이미 존재하는 품질 기록")));
    }

    @Test
    void create_ng_rate_exceeds_threshold_test() throws Exception {
        // given - NG 비율이 0.5%를 초과 (10%)
        QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                testDailyProduction.getId(),
                testProcess.getId(),
                950,
                50  // NG 비율 5%
        );
        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions result = mvc.perform(
                post("/api/quality-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.body.evaluationRequired").value(true))
                .andExpect(jsonPath("$.body.evaluationReason", containsString("NG 비율 임계값 초과")));
    }

    @Test
    void update_as_user_test() throws Exception {
        // given
        QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
        qualityRecordRepository.save(qr);
        Long qrId = qr.getId();

        QualityRecordRequest.Update request = new QualityRecordRequest.Update(850, 150);
        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions result = mvc.perform(
                put("/api/quality-records/" + qrId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.okQuantity").value(850))
                .andExpect(jsonPath("$.body.ngQuantity").value(150))
                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                .andExpect(jsonPath("$.body.ngRate").value(15.0));
    }

    @Test
    void delete_as_manager_test() throws Exception {
        // given
        QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
        qualityRecordRepository.save(qr);
        Long qrId = qr.getId();

        // when
        ResultActions result = mvc.perform(
                delete("/api/quality-records/" + qrId)
                        .header("Authorization", "Bearer " + managerToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.id").value(qrId.intValue()));
    }

    @Test
    void delete_as_user_forbidden_test() throws Exception {
        // given
        QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
        qualityRecordRepository.save(qr);
        Long qrId = qr.getId();

        // when
        ResultActions result = mvc.perform(
                delete("/api/quality-records/" + qrId)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void getEvaluationRequiredList_test() throws Exception {
        // given
        // 평가 필요 항목 (NG 비율 5% > 임계값 0.5%) - Service를 통해 생성하여 evaluationRequired 계산
        QualityRecordRequest.Create request1 = new QualityRecordRequest.Create(
                testDailyProduction.getId(),
                testProcess.getId(),
                950,
                50  // NG 비율 5%
        );
        String requestBody1 = om.writeValueAsString(request1);
        mvc.perform(
                post("/api/quality-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody1)
                        .header("Authorization", "Bearer " + userToken));

        // 평가 불필요 항목 (NG 비율 0.1%)
        LocalDate date2 = LocalDate.of(2025, 1, 16);
        DailyProduction dp2 = new DailyProduction(testItem, date2, 1000);
        dailyProductionRepository.save(dp2);
        QualityRecordRequest.Create request2 = new QualityRecordRequest.Create(
                dp2.getId(),
                testProcess.getId(),
                999,
                1  // NG 비율 0.1%
        );
        String requestBody2 = om.writeValueAsString(request2);
        mvc.perform(
                post("/api/quality-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2)
                        .header("Authorization", "Bearer " + userToken));

        // when
        ResultActions result = mvc.perform(
                get("/api/quality-records/evaluation-required")
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(1))
                .andExpect(jsonPath("$.body[0].evaluationRequired").value(true))
                .andExpect(jsonPath("$.body[0].evaluationReason").exists());
    }

    @Test
    void getEvaluationRequiredList_empty_test() throws Exception {
        // given - 평가 불필요 항목만 존재 (Service를 통해 생성)
        QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                testDailyProduction.getId(),
                testProcess.getId(),
                999,
                1  // NG 비율 0.1%
        );
        String requestBody = om.writeValueAsString(request);
        mvc.perform(
                post("/api/quality-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + userToken));

        // when
        ResultActions result = mvc.perform(
                get("/api/quality-records/evaluation-required")
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(0));
    }
}
