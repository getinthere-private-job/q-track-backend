package dev.dote.qtrack.user;

import static org.hamcrest.Matchers.containsString;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dote.qtrack._core.security.JwtUtil;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@ExtendWith(RestDocumentationExtension.class)
class UserControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mvc;

        private ObjectMapper om = new ObjectMapper();

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtUtil jwtUtil;

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
        }

        @Test
        void signup_test() throws Exception {
                // given - 새로운 사용자 생성 (data-dev.sql에 없는 사용자명 사용)
                UserRequest.Signup request = new UserRequest.Signup("newuser", "password123", Role.USER);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").exists())
                                .andExpect(jsonPath("$.body.username").value("newuser"))
                                .andExpect(jsonPath("$.body.role").value("USER"))
                                .andDo(MockMvcRestDocumentation.document("user-signup",
                                                requestFields(
                                                                fieldWithPath("username").description("사용자명"),
                                                                fieldWithPath("password").description("비밀번호"),
                                                                fieldWithPath("role").description(
                                                                                "권한 (USER, MANAGER, ADMIN)")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("생성된 사용자 ID"),
                                                                fieldWithPath("body.username").description("사용자명"),
                                                                fieldWithPath("body.role").description("권한"))));
        }

        @Test
        void signup_duplicate_username_test() throws Exception {
                // given - data-dev.sql의 testuser가 이미 존재함

                UserRequest.Signup request = new UserRequest.Signup("testuser", "password456", Role.USER);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.msg", containsString("이미 존재하는 사용자명")));
        }

        @Test
        void findById_test() throws Exception {
                // given - data-dev.sql의 testuser 사용
                User user = userRepository.findByUsername("testuser")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 testuser를 찾을 수 없습니다"));
                Long userId = user.getId();
                String token = jwtUtil.generateToken(user.getId(), user.getRole());

                // when
                ResultActions result = mvc.perform(
                                get("/api/users/{id}", userId)
                                                .header("Authorization", "Bearer " + token));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").value(userId.intValue()))
                                .andExpect(jsonPath("$.body.username").value("testuser"))
                                .andExpect(jsonPath("$.body.role").value("USER"))
                                .andDo(MockMvcRestDocumentation.document("user-findById",
                                                pathParameters(
                                                                parameterWithName("id").description("사용자 ID")),
                                                requestHeaders(
                                                                headerWithName("Authorization").description(
                                                                                "JWT 토큰 (Bearer {token})")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.id").description("사용자 ID"),
                                                                fieldWithPath("body.username").description("사용자명"),
                                                                fieldWithPath("body.role").description("권한"))));
        }

        @Test
        void login_success_test() throws Exception {
                // given - data-dev.sql의 testuser 사용 시 BCrypt 해시 검증 문제가 있을 수 있으므로
                // 테스트용 사용자를 새로 생성하여 검증
                User testUser = new User("logintestuser", passwordEncoder.encode("password123"), Role.USER);
                userRepository.save(testUser);

                UserRequest.Login request = new UserRequest.Login("logintestuser", "password123");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.token").exists())
                                .andExpect(jsonPath("$.body.id").value(testUser.getId().intValue()))
                                .andExpect(jsonPath("$.body.username").value("logintestuser"))
                                .andExpect(jsonPath("$.body.role").value("USER"))
                                .andDo(MockMvcRestDocumentation.document("user-login",
                                                requestFields(
                                                                fieldWithPath("username").description("사용자명"),
                                                                fieldWithPath("password").description("비밀번호")),
                                                responseFields(
                                                                fieldWithPath("status").description("HTTP 상태 코드"),
                                                                fieldWithPath("msg").description("응답 메시지"),
                                                                fieldWithPath("body.token").description(
                                                                                "JWT Access Token (5일 유효기간)"),
                                                                fieldWithPath("body.id").description("사용자 ID"),
                                                                fieldWithPath("body.username").description("사용자명"),
                                                                fieldWithPath("body.role").description("권한"))));
        }

        @Test
        void login_wrong_password_test() throws Exception {
                // given - data-dev.sql의 testuser 사용
                User user = userRepository.findByUsername("testuser")
                                .orElseThrow(() -> new RuntimeException("data-dev.sql의 testuser를 찾을 수 없습니다"));

                UserRequest.Login request = new UserRequest.Login("testuser", "wrongpassword");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.msg", containsString("사용자명 또는 비밀번호가 잘못되었습니다")));
        }

        @Test
        void login_user_not_found_test() throws Exception {
                // given
                UserRequest.Login request = new UserRequest.Login("nonexistent", "password123");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.msg", containsString("사용자명 또는 비밀번호가 잘못되었습니다")));
        }
}
