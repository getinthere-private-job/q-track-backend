package dev.dote.qtrack.qualityrecord;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

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
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@ExtendWith(RestDocumentationExtension.class)
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
        void setUp(RestDocumentationContextProvider restDocumentation) {
                qualityRecordRepository.deleteAll();
                dailyProductionRepository.deleteAll();
                processRepository.deleteAll();
                itemRepository.deleteAll();
                systemCodeRepository.deleteAll();
                userRepository.deleteAll();

                mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                                                .operationPreprocessors()
                                                .withRequestDefaults(Preprocessors.prettyPrint())
                                                .withResponseDefaults(Preprocessors.prettyPrint())
                                                .and())
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
                                .andExpect(jsonPath("$.body.length()").value(1))
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-findAll",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body[]").description("품질 기록 목록"),
                                                                fieldWithPath("body[].id").description("품질 기록 ID"),
                                                                fieldWithPath("body[].dailyProductionId").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body[].processId").description("공정 ID"),
                                                                fieldWithPath("body[].okQuantity").description("OK 수량"),
                                                                fieldWithPath("body[].ngQuantity").description("NG 수량"),
                                                                fieldWithPath("body[].totalQuantity").description("총 수량"),
                                                                fieldWithPath("body[].ngRate").description("NG 비율 (%)"),
                                                                fieldWithPath("body[].expertEvaluation").description("전문가 평가"),
                                                                fieldWithPath("body[].evaluationRequired").description("평가 필요 여부"),
                                                                fieldWithPath("body[].evaluationReason").description("평가 필요 사유")
                                                )
                                ));
        }

        @Test
        void findById_test() throws Exception {
                // given
                QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
                qualityRecordRepository.save(qr);
                Long qrId = qr.getId();

                // when
                ResultActions result = mvc.perform(
                                get("/api/quality-records/{id}", qrId)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.id").value(qrId.intValue()))
                                .andExpect(jsonPath("$.body.okQuantity").value(900))
                                .andExpect(jsonPath("$.body.ngQuantity").value(100))
                                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                                .andExpect(jsonPath("$.body.ngRate").value(10.0))
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-findById",
                                                pathParameters(
                                                                parameterWithName("id").description("품질 기록 ID")
                                                ),
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("품질 기록 ID"),
                                                                fieldWithPath("body.dailyProductionId").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body.processId").description("공정 ID"),
                                                                fieldWithPath("body.okQuantity").description("OK 수량"),
                                                                fieldWithPath("body.ngQuantity").description("NG 수량"),
                                                                fieldWithPath("body.totalQuantity").description("총 수량"),
                                                                fieldWithPath("body.ngRate").description("NG 비율 (%)"),
                                                                fieldWithPath("body.expertEvaluation").description("전문가 평가"),
                                                                fieldWithPath("body.evaluationRequired").description("평가 필요 여부"),
                                                                fieldWithPath("body.evaluationReason").description("평가 필요 사유"),
                                                                fieldWithPath("body.evaluatedAt").description("평가 일시"),
                                                                fieldWithPath("body.evaluatedBy").description("평가자 ID")
                                                )
                                ));
        }

        @Test
        void create_as_user_test() throws Exception {
                // given
                QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                900,
                                100);
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
                                .andExpect(jsonPath("$.body.ngRate").value(10.0))
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-create",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                requestFields(
                                                                fieldWithPath("dailyProductionId").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("processId").description("공정 ID"),
                                                                fieldWithPath("okQuantity").description("OK 수량"),
                                                                fieldWithPath("ngQuantity").description("NG 수량")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("생성된 품질 기록 ID"),
                                                                fieldWithPath("body.dailyProductionId").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body.processId").description("공정 ID"),
                                                                fieldWithPath("body.okQuantity").description("OK 수량"),
                                                                fieldWithPath("body.ngQuantity").description("NG 수량"),
                                                                fieldWithPath("body.totalQuantity").description("총 수량"),
                                                                fieldWithPath("body.ngRate").description("NG 비율 (%) - 자동 계산"),
                                                                fieldWithPath("body.evaluationRequired").description("평가 필요 여부 - 자동 판단"),
                                                                fieldWithPath("body.evaluationReason").description("평가 필요 사유")
                                                )
                                ));
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
                                100);
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
                                50 // NG 비율 5%
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
                                put("/api/quality-records/{id}", qrId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.okQuantity").value(850))
                                .andExpect(jsonPath("$.body.ngQuantity").value(150))
                                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                                .andExpect(jsonPath("$.body.ngRate").value(15.0))
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-update",
                                                pathParameters(
                                                                parameterWithName("id").description("품질 기록 ID")
                                                ),
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                requestFields(
                                                                fieldWithPath("okQuantity").description("OK 수량"),
                                                                fieldWithPath("ngQuantity").description("NG 수량")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("품질 기록 ID"),
                                                                fieldWithPath("body.dailyProductionId").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body.processId").description("공정 ID"),
                                                                fieldWithPath("body.okQuantity").description("OK 수량"),
                                                                fieldWithPath("body.ngQuantity").description("NG 수량"),
                                                                fieldWithPath("body.totalQuantity").description("총 수량"),
                                                                fieldWithPath("body.ngRate").description("NG 비율 (%) - 자동 계산"),
                                                                fieldWithPath("body.evaluationRequired").description("평가 필요 여부 - 자동 판단"),
                                                                fieldWithPath("body.evaluationReason").description("평가 필요 사유")
                                                )
                                ));
        }

        @Test
        void delete_as_manager_test() throws Exception {
                // given
                QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
                qualityRecordRepository.save(qr);
                Long qrId = qr.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/quality-records/{id}", qrId)
                                                .header("Authorization", "Bearer " + managerToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.id").value(qrId.intValue()))
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-delete",
                                                pathParameters(
                                                                parameterWithName("id").description("품질 기록 ID")
                                                ),
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token}) - MANAGER 이상 권한 필요")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("삭제된 품질 기록 ID")
                                                )
                                ));
        }

        @Test
        void delete_as_user_forbidden_test() throws Exception {
                // given
                QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
                qualityRecordRepository.save(qr);
                Long qrId = qr.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/quality-records/{id}", qrId)
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
                                50 // NG 비율 5%
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
                                1 // NG 비율 0.1%
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
                                .andExpect(jsonPath("$.body[0].evaluationReason").exists())
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-evaluation-required",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body[]").description("평가 필요 품질 기록 목록"),
                                                                fieldWithPath("body[].id").description("품질 기록 ID"),
                                                                fieldWithPath("body[].dailyProductionId").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body[].processId").description("공정 ID"),
                                                                fieldWithPath("body[].okQuantity").description("OK 수량"),
                                                                fieldWithPath("body[].ngQuantity").description("NG 수량"),
                                                                fieldWithPath("body[].totalQuantity").description("총 수량"),
                                                                fieldWithPath("body[].ngRate").description("NG 비율 (%)"),
                                                                fieldWithPath("body[].expertEvaluation").description("전문가 평가"),
                                                                fieldWithPath("body[].evaluationRequired").description("평가 필요 여부 (true)"),
                                                                fieldWithPath("body[].evaluationReason").description("평가 필요 사유")
                                                )
                                ));
        }

        @Test
        void getEvaluationRequiredList_empty_test() throws Exception {
                // given - 평가 불필요 항목만 존재 (Service를 통해 생성)
                QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                999,
                                1 // NG 비율 0.1%
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

        @Test
        void ngRate_auto_calculation_test() throws Exception {
                // given - ngRate 자동 계산 검증
                QualityRecordRequest.Create request = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                950,
                                50 // NG 비율 = 50/1000 * 100 = 5.0%
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
                                .andExpect(jsonPath("$.body.ngRate").value(5.0)); // 자동 계산된 NG 비율 검증
        }

        @Test
        void evaluation_required_increase_rate_test() throws Exception {
                // given - 전일 대비 NG 비율 급증 테스트
                // 전일: NG 비율 1% (10/1000)
                LocalDate yesterday = LocalDate.of(2025, 1, 14);
                DailyProduction dpYesterday = new DailyProduction(testItem, yesterday, 1000);
                dailyProductionRepository.save(dpYesterday);
                QualityRecordRequest.Create requestYesterday = new QualityRecordRequest.Create(
                                dpYesterday.getId(),
                                testProcess.getId(),
                                990,
                                10 // NG 비율 1%
                );
                String requestBodyYesterday = om.writeValueAsString(requestYesterday);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBodyYesterday)
                                                .header("Authorization", "Bearer " + userToken));

                // 오늘: NG 비율 5% (50/1000) - 전일 대비 5배 증가 (2배 이상이므로 평가 필요)
                QualityRecordRequest.Create requestToday = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                950,
                                50 // NG 비율 5%
                );
                String requestBodyToday = om.writeValueAsString(requestToday);

                // when
                ResultActions result = mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBodyToday)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.body.evaluationRequired").value(true))
                                .andExpect(jsonPath("$.body.evaluationReason", containsString("전일 대비 급증")));
        }

        @Test
        void evaluate_test() throws Exception {
                // given
                QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
                qualityRecordRepository.save(qr);
                Long qrId = qr.getId();

                User evaluator = userRepository.findByUsername("testuser").orElseThrow();
                Long evaluatorId = evaluator.getId();

                QualityRecordRequest.Evaluate request = new QualityRecordRequest.Evaluate("재료 품질 이슈로 판단됨");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                put("/api/quality-records/{id}/evaluate", qrId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.expertEvaluation").value("재료 품질 이슈로 판단됨"))
                                .andExpect(jsonPath("$.body.evaluatedBy").value(evaluatorId.intValue())) // User 외래키 검증
                                .andExpect(jsonPath("$.body.evaluatedAt").exists());
        }

        @Test
        void evaluate_evaluatedBy_user_foreign_key_test() throws Exception {
                // given - evaluatedBy가 User 외래키로 저장되는지 검증
                QualityRecord qr = new QualityRecord(testDailyProduction, testProcess, 900, 100);
                qualityRecordRepository.save(qr);
                Long qrId = qr.getId();

                User evaluator = userRepository.findByUsername("testuser").orElseThrow();
                Long evaluatorId = evaluator.getId();

                QualityRecordRequest.Evaluate request = new QualityRecordRequest.Evaluate("평가 내용");
                String requestBody = om.writeValueAsString(request);

                // when
                mvc.perform(
                                put("/api/quality-records/{id}/evaluate", qrId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then - DB에서 직접 조회하여 evaluatedBy가 User 엔티티로 저장되었는지 확인
                QualityRecord savedQr = qualityRecordRepository.findByIdWithJoins(qrId).orElseThrow();
                assert savedQr.getEvaluatedBy() != null;
                assert savedQr.getEvaluatedBy().getId().equals(evaluatorId);
                assert savedQr.getEvaluatedBy().getUsername().equals("testuser");
        }

        @Test
        void getNgRateByProcess_test() throws Exception {
                // given - 여러 공정에 대한 품질 기록 생성
                Process process2 = new Process("P", "제조", "제조 공정", 2);
                processRepository.save(process2);

                // 공정1 (W): NG 100/1000 = 10%
                QualityRecordRequest.Create request1 = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                900,
                                100);
                String requestBody1 = om.writeValueAsString(request1);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody1)
                                                .header("Authorization", "Bearer " + userToken));

                // 공정2 (P): NG 50/500 = 10%
                LocalDate date2 = LocalDate.of(2025, 1, 16);
                DailyProduction dp2 = new DailyProduction(testItem, date2, 500);
                dailyProductionRepository.save(dp2);
                QualityRecordRequest.Create request2 = new QualityRecordRequest.Create(
                                dp2.getId(),
                                process2.getId(),
                                450,
                                50);
                String requestBody2 = om.writeValueAsString(request2);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody2)
                                                .header("Authorization", "Bearer " + userToken));

                // when
                ResultActions result = mvc.perform(
                                get("/api/quality-records/statistics/by-process")
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body").isArray())
                                .andExpect(jsonPath("$.body.length()").value(2))
                                .andExpect(jsonPath("$.body[0].processCode").exists())
                                .andExpect(jsonPath("$.body[0].totalNgQuantity").exists())
                                .andExpect(jsonPath("$.body[0].totalQuantity").exists())
                                .andExpect(jsonPath("$.body[0].ngRate").exists())
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-statistics-by-process",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                queryParameters(
                                                                parameterWithName("startDate").optional().description("통계 시작일 (yyyy-MM-dd, 선택 사항)"),
                                                                parameterWithName("endDate").optional().description("통계 종료일 (yyyy-MM-dd, 선택 사항)")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body[]").description("공정별 NG 비율 통계 목록"),
                                                                fieldWithPath("body[].processId").description("공정 ID"),
                                                                fieldWithPath("body[].processCode").description("공정 코드"),
                                                                fieldWithPath("body[].processName").description("공정명"),
                                                                fieldWithPath("body[].totalNgQuantity").description("총 NG 수량"),
                                                                fieldWithPath("body[].totalQuantity").description("총 수량"),
                                                                fieldWithPath("body[].ngRate").description("NG 비율 (%)")
                                                )
                                ));
        }

        @Test
        void getNgRateByProcess_with_date_filter_test() throws Exception {
                // given
                Process process2 = new Process("P", "제조", "제조 공정", 2);
                processRepository.save(process2);

                // 2025-01-15 데이터
                QualityRecordRequest.Create request1 = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                900,
                                100);
                String requestBody1 = om.writeValueAsString(request1);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody1)
                                                .header("Authorization", "Bearer " + userToken));

                // 2025-01-20 데이터 (필터 범위 밖)
                LocalDate date2 = LocalDate.of(2025, 1, 20);
                DailyProduction dp2 = new DailyProduction(testItem, date2, 500);
                dailyProductionRepository.save(dp2);
                QualityRecordRequest.Create request2 = new QualityRecordRequest.Create(
                                dp2.getId(),
                                process2.getId(),
                                450,
                                50);
                String requestBody2 = om.writeValueAsString(request2);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody2)
                                                .header("Authorization", "Bearer " + userToken));

                // when - 2025-01-15 ~ 2025-01-16 범위로 필터링
                ResultActions result = mvc.perform(
                                get("/api/quality-records/statistics/by-process")
                                                .param("startDate", "2025-01-15")
                                                .param("endDate", "2025-01-16")
                                                .header("Authorization", "Bearer " + userToken));

                // then - 2025-01-15 데이터만 포함되어야 함
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.body").isArray())
                                .andExpect(jsonPath("$.body.length()").value(1))
                                .andExpect(jsonPath("$.body[0].processCode").value("W"));
        }

        @Test
        void getNgRateByItem_test() throws Exception {
                // given - 여러 부품에 대한 품질 기록 생성
                Item item2 = new Item("ITEM002", "부품2", "부품2 설명", "카테고리2");
                itemRepository.save(item2);

                LocalDate date2 = LocalDate.of(2025, 1, 16);
                DailyProduction dp2 = new DailyProduction(item2, date2, 500);
                dailyProductionRepository.save(dp2);

                // 부품1: NG 100/1000 = 10%
                QualityRecordRequest.Create request1 = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                900,
                                100);
                String requestBody1 = om.writeValueAsString(request1);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody1)
                                                .header("Authorization", "Bearer " + userToken));

                // 부품2: NG 50/500 = 10%
                QualityRecordRequest.Create request2 = new QualityRecordRequest.Create(
                                dp2.getId(),
                                testProcess.getId(),
                                450,
                                50);
                String requestBody2 = om.writeValueAsString(request2);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody2)
                                                .header("Authorization", "Bearer " + userToken));

                // when
                ResultActions result = mvc.perform(
                                get("/api/quality-records/statistics/by-item")
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body").isArray())
                                .andExpect(jsonPath("$.body.length()").value(2))
                                .andExpect(jsonPath("$.body[0].itemCode").exists())
                                .andExpect(jsonPath("$.body[0].totalNgQuantity").exists())
                                .andExpect(jsonPath("$.body[0].totalQuantity").exists())
                                .andExpect(jsonPath("$.body[0].ngRate").exists())
                                .andDo(MockMvcRestDocumentation.document("qualityrecord-statistics-by-item",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                queryParameters(
                                                                parameterWithName("startDate").optional().description("통계 시작일 (yyyy-MM-dd, 선택 사항)"),
                                                                parameterWithName("endDate").optional().description("통계 종료일 (yyyy-MM-dd, 선택 사항)")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body[]").description("부품별 NG 비율 통계 목록"),
                                                                fieldWithPath("body[].itemId").description("부품 ID"),
                                                                fieldWithPath("body[].itemCode").description("부품 코드"),
                                                                fieldWithPath("body[].itemName").description("부품명"),
                                                                fieldWithPath("body[].totalNgQuantity").description("총 NG 수량"),
                                                                fieldWithPath("body[].totalQuantity").description("총 수량"),
                                                                fieldWithPath("body[].ngRate").description("NG 비율 (%)")
                                                )
                                ));
        }

        @Test
        void getNgRateByItem_with_date_filter_test() throws Exception {
                // given
                Item item2 = new Item("ITEM002", "부품2", "부품2 설명", "카테고리2");
                itemRepository.save(item2);

                // 2025-01-15 데이터
                QualityRecordRequest.Create request1 = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                900,
                                100);
                String requestBody1 = om.writeValueAsString(request1);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody1)
                                                .header("Authorization", "Bearer " + userToken));

                // 2025-01-20 데이터 (필터 범위 밖)
                LocalDate date2 = LocalDate.of(2025, 1, 20);
                DailyProduction dp2 = new DailyProduction(item2, date2, 500);
                dailyProductionRepository.save(dp2);
                QualityRecordRequest.Create request2 = new QualityRecordRequest.Create(
                                dp2.getId(),
                                testProcess.getId(),
                                450,
                                50);
                String requestBody2 = om.writeValueAsString(request2);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody2)
                                                .header("Authorization", "Bearer " + userToken));

                // when - 2025-01-15 ~ 2025-01-16 범위로 필터링
                ResultActions result = mvc.perform(
                                get("/api/quality-records/statistics/by-item")
                                                .param("startDate", "2025-01-15")
                                                .param("endDate", "2025-01-16")
                                                .header("Authorization", "Bearer " + userToken));

                // then - 2025-01-15 데이터만 포함되어야 함
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.body").isArray())
                                .andExpect(jsonPath("$.body.length()").value(1))
                                .andExpect(jsonPath("$.body[0].itemCode").value("ITEM001"));
        }

        @Test
        void getNgRateByProcess_calculation_accuracy_test() throws Exception {
                // given - 정확한 계산 검증을 위한 데이터
                // 공정1: NG 100/1000 = 10%, NG 50/500 = 10% -> 합계: NG 150/1500 = 10%
                Process process2 = new Process("P", "제조", "제조 공정", 2);
                processRepository.save(process2);

                QualityRecordRequest.Create request1 = new QualityRecordRequest.Create(
                                testDailyProduction.getId(),
                                testProcess.getId(),
                                900,
                                100 // NG 10%
                );
                String requestBody1 = om.writeValueAsString(request1);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody1)
                                                .header("Authorization", "Bearer " + userToken));

                LocalDate date2 = LocalDate.of(2025, 1, 16);
                DailyProduction dp2 = new DailyProduction(testItem, date2, 500);
                dailyProductionRepository.save(dp2);
                QualityRecordRequest.Create request2 = new QualityRecordRequest.Create(
                                dp2.getId(),
                                testProcess.getId(),
                                450,
                                50 // NG 10%
                );
                String requestBody2 = om.writeValueAsString(request2);
                mvc.perform(
                                post("/api/quality-records")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody2)
                                                .header("Authorization", "Bearer " + userToken));

                // when
                ResultActions result = mvc.perform(
                                get("/api/quality-records/statistics/by-process")
                                                .header("Authorization", "Bearer " + userToken));

                // then - 공정1의 통계: NG 150/1500 = 10%
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.body[?(@.processCode == 'W')].totalNgQuantity").value(150))
                                .andExpect(jsonPath("$.body[?(@.processCode == 'W')].totalQuantity").value(1500))
                                .andExpect(jsonPath("$.body[?(@.processCode == 'W')].ngRate").value(10.0));
        }
}
