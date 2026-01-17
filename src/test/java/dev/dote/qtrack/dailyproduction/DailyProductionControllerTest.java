package dev.dote.qtrack.dailyproduction;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

import java.time.LocalDate;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.dote.qtrack._core.security.JwtUtil;
import dev.dote.qtrack.item.Item;
import dev.dote.qtrack.item.ItemRepository;
import dev.dote.qtrack.user.Role;
import dev.dote.qtrack.user.User;
import dev.dote.qtrack.user.UserRepository;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@ExtendWith(RestDocumentationExtension.class)
class DailyProductionControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mvc;

        private ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

        @Autowired
        private DailyProductionRepository dailyProductionRepository;

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtUtil jwtUtil;

        private String userToken;
        private String managerToken;
        private String adminToken;
        private Item testItem;

        @BeforeEach
        void setUp(RestDocumentationContextProvider restDocumentation) {
                dailyProductionRepository.deleteAll();
                itemRepository.deleteAll();
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
                User admin = new User("testadmin", passwordEncoder.encode("password123"), Role.ADMIN);
                userRepository.save(user);
                userRepository.save(manager);
                userRepository.save(admin);

                userToken = jwtUtil.generateToken(user.getId(), user.getRole());
                managerToken = jwtUtil.generateToken(manager.getId(), manager.getRole());
                adminToken = jwtUtil.generateToken(admin.getId(), admin.getRole());

                // 테스트용 부품 생성
                testItem = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
                itemRepository.save(testItem);
        }

        @Test
        void findAll_test() throws Exception {
                // given
                LocalDate date1 = LocalDate.of(2025, 1, 15);
                LocalDate date2 = LocalDate.of(2025, 1, 16);
                DailyProduction dp1 = new DailyProduction(testItem, date1, 1000);
                DailyProduction dp2 = new DailyProduction(testItem, date2, 2000);
                dailyProductionRepository.save(dp1);
                dailyProductionRepository.save(dp2);

                // when
                ResultActions result = mvc.perform(
                                get("/api/daily-productions")
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body").isArray())
                                .andExpect(jsonPath("$.body.length()").value(2))
                                .andDo(MockMvcRestDocumentation.document("dailyproduction-findAll",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body[]").description("일별 생산 데이터 목록"),
                                                                fieldWithPath("body[].id").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body[].itemId").description("부품 ID"),
                                                                fieldWithPath("body[].productionDate").description("생산일"),
                                                                fieldWithPath("body[].totalQuantity").description("총 생산 수량")
                                                )
                                ));
        }

        @Test
        void findById_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProduction dp = new DailyProduction(testItem, date, 1000);
                dailyProductionRepository.save(dp);
                Long dpId = dp.getId();

                // when
                ResultActions result = mvc.perform(
                                get("/api/daily-productions/{id}", dpId)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").value(dpId.intValue()))
                                .andExpect(jsonPath("$.body.itemId").value(testItem.getId().intValue()))
                                .andExpect(jsonPath("$.body.productionDate").value("2025-01-15"))
                                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                                .andDo(MockMvcRestDocumentation.document("dailyproduction-findById",
                                                pathParameters(
                                                                parameterWithName("id").description("일별 생산 데이터 ID")
                                                ),
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body.itemId").description("부품 ID"),
                                                                fieldWithPath("body.productionDate").description("생산일"),
                                                                fieldWithPath("body.totalQuantity").description("총 생산 수량")
                                                )
                                ));
        }

        @Test
        void create_as_user_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProductionRequest.Create request = new DailyProductionRequest.Create(
                                testItem.getId(),
                                date,
                                1000);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/daily-productions")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.id").exists())
                                .andExpect(jsonPath("$.body.itemId").value(testItem.getId().intValue()))
                                .andExpect(jsonPath("$.body.productionDate").value("2025-01-15"))
                                .andExpect(jsonPath("$.body.totalQuantity").value(1000))
                                .andDo(MockMvcRestDocumentation.document("dailyproduction-create",
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                requestFields(
                                                                fieldWithPath("itemId").description("부품 ID"),
                                                                fieldWithPath("productionDate").description("생산일 (yyyy-MM-dd)"),
                                                                fieldWithPath("totalQuantity").description("총 생산 수량")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("생성된 일별 생산 데이터 ID"),
                                                                fieldWithPath("body.itemId").description("부품 ID"),
                                                                fieldWithPath("body.productionDate").description("생산일"),
                                                                fieldWithPath("body.totalQuantity").description("총 생산 수량")
                                                )
                                ));
        }

        @Test
        void create_duplicate_item_date_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProduction existing = new DailyProduction(testItem, date, 1000);
                dailyProductionRepository.save(existing);

                DailyProductionRequest.Create request = new DailyProductionRequest.Create(
                                testItem.getId(),
                                date,
                                2000);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/daily-productions")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.msg", containsString("이미 존재하는 일별 생산 데이터")));
        }

        @Test
        void create_invalid_item_id_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProductionRequest.Create request = new DailyProductionRequest.Create(
                                999L,
                                date,
                                1000);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/daily-productions")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.msg", containsString("부품을 찾을 수 없습니다")));
        }

        @Test
        void update_as_user_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProduction dp = new DailyProduction(testItem, date, 1000);
                dailyProductionRepository.save(dp);
                Long dpId = dp.getId();

                DailyProductionRequest.Update request = new DailyProductionRequest.Update(2000);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                put("/api/daily-productions/{id}", dpId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.totalQuantity").value(2000))
                                .andDo(MockMvcRestDocumentation.document("dailyproduction-update",
                                                pathParameters(
                                                                parameterWithName("id").description("일별 생산 데이터 ID")
                                                ),
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                                                ),
                                                requestFields(
                                                                fieldWithPath("totalQuantity").description("총 생산 수량")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("일별 생산 데이터 ID"),
                                                                fieldWithPath("body.itemId").description("부품 ID"),
                                                                fieldWithPath("body.productionDate").description("생산일"),
                                                                fieldWithPath("body.totalQuantity").description("총 생산 수량")
                                                )
                                ));
        }

        @Test
        void delete_as_manager_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProduction dp = new DailyProduction(testItem, date, 1000);
                dailyProductionRepository.save(dp);
                Long dpId = dp.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/daily-productions/{id}", dpId)
                                                .header("Authorization", "Bearer " + managerToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.id").value(dpId.intValue()))
                                .andDo(MockMvcRestDocumentation.document("dailyproduction-delete",
                                                pathParameters(
                                                                parameterWithName("id").description("일별 생산 데이터 ID")
                                                ),
                                                requestHeaders(
                                                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token}) - MANAGER 이상 권한 필요")
                                                ),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("삭제된 일별 생산 데이터 ID")
                                                )
                                ));
        }

        @Test
        void delete_as_user_forbidden_test() throws Exception {
                // given
                LocalDate date = LocalDate.of(2025, 1, 15);
                DailyProduction dp = new DailyProduction(testItem, date, 1000);
                dailyProductionRepository.save(dp);
                Long dpId = dp.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/daily-productions/{id}", dpId)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isForbidden());
        }
}
