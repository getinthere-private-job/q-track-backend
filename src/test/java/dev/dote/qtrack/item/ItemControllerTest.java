package dev.dote.qtrack.item;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import dev.dote.qtrack._core.security.JwtUtil;
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
class ItemControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mvc;

        private ObjectMapper om = new ObjectMapper();

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

        @BeforeEach
        void setUp(RestDocumentationContextProvider restDocumentation) {
                mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                                                .operationPreprocessors()
                                                .withRequestDefaults(Preprocessors.prettyPrint())
                                                .withResponseDefaults(Preprocessors.prettyPrint())
                                                .and())
                                .build();

                // data-dev.sql의 사용자 조회 및 토큰 생성
                User user = userRepository.findByUsername("testuser")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 testuser를 찾을 수 없습니다"));
                User manager = userRepository.findByUsername("testmanager")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 testmanager를 찾을 수 없습니다"));
                User admin = userRepository.findByUsername("testadmin")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 testadmin를 찾을 수 없습니다"));

                userToken = jwtUtil.generateToken(user.getId(), user.getRole());
                managerToken = jwtUtil.generateToken(manager.getId(), manager.getRole());
                adminToken = jwtUtil.generateToken(admin.getId(), admin.getRole());
        }

        @Test
        void findAll_test() throws Exception {
                // given - data-dev.sql의 데이터 사용 (ITEM001, ITEM002, ITEM003, ITEM004, ITEM005)

                // when
                ResultActions result = mvc.perform(
                                get("/api/items")
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body").isArray())
                                .andExpect(jsonPath("$.body.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                                .andDo(MockMvcRestDocumentation.document("item-findAll",
                                                requestHeaders(
                                                                headerWithName("Authorization").description(
                                                                                "JWT 토큰 (Bearer {token})")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body[]").description("부품 목록"),
                                                                fieldWithPath("body[].id").description("부품 ID"),
                                                                fieldWithPath("body[].code").description("부품 코드"),
                                                                fieldWithPath("body[].name").description("부품명"),
                                                                fieldWithPath("body[].description").description("설명"),
                                                                fieldWithPath("body[].category").description("카테고리"))));
        }

        @Test
        void findById_test() throws Exception {
                // given - data-dev.sql의 ITEM001 사용
                Item item = itemRepository.findByCode("ITEM001")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 ITEM001를 찾을 수 없습니다"));
                Long itemId = item.getId();

                // when
                ResultActions result = mvc.perform(
                                get("/api/items/{id}", itemId)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").value(itemId.intValue()))
                                .andExpect(jsonPath("$.body.code").value("ITEM001"))
                                .andExpect(jsonPath("$.body.name").value("P2 부품"))
                                .andExpect(jsonPath("$.body.description").value("엔진 제어 부품"))
                                .andExpect(jsonPath("$.body.category").value("엔진"))
                                .andDo(MockMvcRestDocumentation.document("item-findById",
                                                pathParameters(
                                                                parameterWithName("id").description("부품 ID")),
                                                requestHeaders(
                                                                headerWithName("Authorization").description(
                                                                                "JWT 토큰 (Bearer {token})")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("부품 ID"),
                                                                fieldWithPath("body.code").description("부품 코드"),
                                                                fieldWithPath("body.name").description("부품명"),
                                                                fieldWithPath("body.description").description("설명"),
                                                                fieldWithPath("body.category").description("카테고리"))));
        }

        @Test
        void create_as_manager_test() throws Exception {
                // given - data-dev.sql에 없는 새로운 코드 사용
                ItemRequest.Create request = new ItemRequest.Create("ITEM999", "새 부품", "새 부품 설명", "새 카테고리");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/items")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + managerToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").exists())
                                .andExpect(jsonPath("$.body.code").value("ITEM999"))
                                .andExpect(jsonPath("$.body.name").value("새 부품"))
                                .andDo(MockMvcRestDocumentation.document("item-create",
                                                requestHeaders(
                                                                headerWithName("Authorization").description(
                                                                                "JWT 토큰 (Bearer {token}) - MANAGER 이상 권한 필요")),
                                                requestFields(
                                                                fieldWithPath("code").description("부품 코드"),
                                                                fieldWithPath("name").description("부품명"),
                                                                fieldWithPath("description").description("설명"),
                                                                fieldWithPath("category").description("카테고리")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("생성된 부품 ID"),
                                                                fieldWithPath("body.code").description("부품 코드"),
                                                                fieldWithPath("body.name").description("부품명"),
                                                                fieldWithPath("body.description").description("설명"),
                                                                fieldWithPath("body.category").description("카테고리"))));
        }

        @Test
        void create_as_admin_test() throws Exception {
                // given - data-dev.sql에 없는 새로운 코드 사용
                ItemRequest.Create request = new ItemRequest.Create("ITEM998", "새 부품", "새 부품 설명", "새 카테고리");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/items")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + adminToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.code").value("ITEM998"));
        }

        @Test
        void create_as_user_forbidden_test() throws Exception {
                // given
                ItemRequest.Create request = new ItemRequest.Create("ITEM001", "부품1", "부품1 설명", "카테고리1");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/items")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isForbidden());
        }

        @Test
        void create_duplicate_code_test() throws Exception {
                // given - data-dev.sql의 ITEM001이 이미 존재함

                ItemRequest.Create request = new ItemRequest.Create("ITEM001", "부품2", "부품2 설명", "카테고리2");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/items")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + managerToken));

                // then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.msg", containsString("이미 존재하는 부품 코드")));
        }

        @Test
        void update_as_manager_test() throws Exception {
                // given - data-dev.sql의 ITEM001 사용
                Item item = itemRepository.findByCode("ITEM001")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 ITEM001를 찾을 수 없습니다"));
                Long itemId = item.getId();

                ItemRequest.Update request = new ItemRequest.Update("부품1 수정", "수정된 설명", "수정된 카테고리");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                put("/api/items/{id}", itemId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + managerToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.name").value("부품1 수정"))
                                .andExpect(jsonPath("$.body.description").value("수정된 설명"))
                                .andExpect(jsonPath("$.body.category").value("수정된 카테고리"))
                                .andDo(MockMvcRestDocumentation.document("item-update",
                                                pathParameters(
                                                                parameterWithName("id").description("부품 ID")),
                                                requestHeaders(
                                                                headerWithName("Authorization").description(
                                                                                "JWT 토큰 (Bearer {token}) - MANAGER 이상 권한 필요")),
                                                requestFields(
                                                                fieldWithPath("name").description("부품명"),
                                                                fieldWithPath("description").description("설명"),
                                                                fieldWithPath("category").description("카테고리")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("부품 ID"),
                                                                fieldWithPath("body.code").description("부품 코드"),
                                                                fieldWithPath("body.name").description("부품명"),
                                                                fieldWithPath("body.description").description("설명"),
                                                                fieldWithPath("body.category").description("카테고리"))));
        }

        @Test
        void update_as_user_forbidden_test() throws Exception {
                // given - data-dev.sql의 ITEM001 사용
                Item item = itemRepository.findByCode("ITEM001")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 ITEM001를 찾을 수 없습니다"));
                Long itemId = item.getId();

                ItemRequest.Update request = new ItemRequest.Update("부품1 수정", "수정된 설명", "수정된 카테고리");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                put("/api/items/{id}", itemId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isForbidden());
        }

        @Test
        void delete_as_admin_test() throws Exception {
                // given - data-dev.sql의 ITEM001 사용
                Item item = itemRepository.findByCode("ITEM001")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 ITEM001를 찾을 수 없습니다"));
                Long itemId = item.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/items/{id}", itemId)
                                                .header("Authorization", "Bearer " + adminToken));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.body.id").value(itemId.intValue()))
                                .andDo(MockMvcRestDocumentation.document("item-delete",
                                                pathParameters(
                                                                parameterWithName("id").description("부품 ID")),
                                                requestHeaders(
                                                                headerWithName("Authorization").description(
                                                                                "JWT 토큰 (Bearer {token}) - ADMIN 권한 필요")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("삭제된 부품 ID"))));
        }

        @Test
        void delete_as_manager_forbidden_test() throws Exception {
                // given - data-dev.sql의 ITEM001 사용
                Item item = itemRepository.findByCode("ITEM001")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 ITEM001를 찾을 수 없습니다"));
                Long itemId = item.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/items/{id}", itemId)
                                                .header("Authorization", "Bearer " + managerToken));

                // then
                result.andExpect(status().isForbidden());
        }

        @Test
        void delete_as_user_forbidden_test() throws Exception {
                // given - data-dev.sql의 ITEM001 사용
                Item item = itemRepository.findByCode("ITEM001")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 ITEM001를 찾을 수 없습니다"));
                Long itemId = item.getId();

                // when
                ResultActions result = mvc.perform(
                                delete("/api/items/{id}", itemId)
                                                .header("Authorization", "Bearer " + userToken));

                // then
                result.andExpect(status().isForbidden());
        }
}
