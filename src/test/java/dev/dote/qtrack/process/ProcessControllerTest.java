package dev.dote.qtrack.process;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

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
class ProcessControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

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
        token = jwtUtil.generateToken(user.getId(), user.getRole());
    }

    @Test
    void findAll_test() throws Exception {
        // given - data-dev.sql의 데이터 사용 (W, P, 검)

        // when
        ResultActions result = mvc.perform(
                get("/api/processes")
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(3))
                .andExpect(jsonPath("$.body[0].code").value("W"))
                .andExpect(jsonPath("$.body[0].name").value("작업"))
                .andExpect(jsonPath("$.body[1].code").value("P"))
                .andExpect(jsonPath("$.body[2].code").value("검"))
                .andDo(MockMvcRestDocumentation.document("process-findAll",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                        ),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("msg").description("응답 메시지"),
                                fieldWithPath("body[]").description("공정 목록"),
                                fieldWithPath("body[].id").description("공정 ID"),
                                fieldWithPath("body[].code").description("공정 코드"),
                                fieldWithPath("body[].name").description("공정명"),
                                fieldWithPath("body[].description").description("설명"),
                                fieldWithPath("body[].sequence").description("순서")
                        )
                ));
    }

    @Test
    void findById_test() throws Exception {
        // given - data-dev.sql의 'W' 공정 사용
        Process process = processRepository.findByCode("W")
                .orElseThrow(() -> new RuntimeException("data-dev.sql의 'W' 공정을 찾을 수 없습니다"));
        Long processId = process.getId();

        // when
        ResultActions result = mvc.perform(
                get("/api/processes/{id}", processId)
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body.id").value(processId.intValue()))
                .andExpect(jsonPath("$.body.code").value("W"))
                .andExpect(jsonPath("$.body.name").value("작업"))
                .andExpect(jsonPath("$.body.description").value("초기 가공 작업 공정"))
                .andExpect(jsonPath("$.body.sequence").value(1))
                .andDo(MockMvcRestDocumentation.document("process-findById",
                        pathParameters(
                                parameterWithName("id").description("공정 ID")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")
                        ),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("msg").description("응답 메시지"),
                                fieldWithPath("body.id").description("공정 ID"),
                                fieldWithPath("body.code").description("공정 코드"),
                                fieldWithPath("body.name").description("공정명"),
                                fieldWithPath("body.description").description("설명"),
                                fieldWithPath("body.sequence").description("순서")
                        )
                ));
    }

    @Test
    void findById_not_found_test() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when
        ResultActions result = mvc.perform(
                get("/api/processes/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.msg", containsString("공정을 찾을 수 없습니다")));
    }
}
