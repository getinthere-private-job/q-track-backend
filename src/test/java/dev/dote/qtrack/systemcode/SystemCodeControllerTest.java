package dev.dote.qtrack.systemcode;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
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
class SystemCodeControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    private SystemCodeRepository systemCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        systemCodeRepository.deleteAll();
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
        userRepository.save(user);
        token = jwtUtil.generateToken(user.getId(), user.getRole());
    }

    @Test
    void findAll_test() throws Exception {
        // given
        SystemCode code1 = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD", "0.5", "NG 비율 임계값", true);
        SystemCode code2 = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_ALERT", "1.0", "NG 비율 경고", true);
        SystemCode code3 = new SystemCode("EVALUATION", "INCREASE_RATE_THRESHOLD", "2.0", "증가율 임계값", true);
        systemCodeRepository.save(code1);
        systemCodeRepository.save(code2);
        systemCodeRepository.save(code3);

        // when
        ResultActions result = mvc.perform(
                get("/api/system-codes")
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(3))
                .andDo(MockMvcRestDocumentation.document("systemcode-findAll",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("msg").description("응답 메시지"),
                                fieldWithPath("body[]").description("시스템 코드 목록"),
                                fieldWithPath("body[].id").description("시스템 코드 ID"),
                                fieldWithPath("body[].codeGroup").description("코드 그룹"),
                                fieldWithPath("body[].codeKey").description("코드 키"),
                                fieldWithPath("body[].codeValue").description("코드 값"),
                                fieldWithPath("body[].description").description("설명"),
                                fieldWithPath("body[].isActive").description("활성화 여부"))));
    }

    @Test
    void findAll_by_codeGroup_test() throws Exception {
        // given
        SystemCode code1 = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD", "0.5", "NG 비율 임계값", true);
        SystemCode code2 = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_ALERT", "1.0", "NG 비율 경고", true);
        SystemCode code3 = new SystemCode("EVALUATION", "INCREASE_RATE_THRESHOLD", "2.0", "증가율 임계값", true);
        systemCodeRepository.save(code1);
        systemCodeRepository.save(code2);
        systemCodeRepository.save(code3);

        // when
        ResultActions result = mvc.perform(
                get("/api/system-codes")
                        .param("codeGroup", "INDUSTRY_AVERAGE")
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(2))
                .andExpect(jsonPath("$.body[0].codeGroup").value("INDUSTRY_AVERAGE"))
                .andExpect(jsonPath("$.body[1].codeGroup").value("INDUSTRY_AVERAGE"))
                .andDo(MockMvcRestDocumentation.document("systemcode-findAll-by-codeGroup",
                        queryParameters(
                                parameterWithName("codeGroup").description("코드 그룹 (선택 사항)")),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 토큰 (Bearer {token})")),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("msg").description("응답 메시지"),
                                fieldWithPath("body[]").description("시스템 코드 목록"),
                                fieldWithPath("body[].id").description("시스템 코드 ID"),
                                fieldWithPath("body[].codeGroup").description("코드 그룹"),
                                fieldWithPath("body[].codeKey").description("코드 키"),
                                fieldWithPath("body[].codeValue").description("코드 값"),
                                fieldWithPath("body[].description").description("설명"),
                                fieldWithPath("body[].isActive").description("활성화 여부"))));
    }

    @Test
    void getCodeValue_test() throws Exception {
        // given
        SystemCode code = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD", "0.5", "NG 비율 임계값", true);
        systemCodeRepository.save(code);

        // when
        String codeValue = systemCodeRepository.findByCodeGroupAndCodeKey("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD")
                .map(SystemCode::getCodeValue)
                .orElse(null);

        // then
        assert codeValue != null;
        assert codeValue.equals("0.5");
    }

    @Test
    void getCodeValue_not_found_test() throws Exception {
        // given
        SystemCode code = new SystemCode("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD", "0.5", "NG 비율 임계값", true);
        systemCodeRepository.save(code);

        // when & then
        try {
            systemCodeRepository.findByCodeGroupAndCodeKey("INDUSTRY_AVERAGE", "NON_EXISTENT")
                    .map(SystemCode::getCodeValue)
                    .orElseThrow(() -> new RuntimeException("시스템 코드를 찾을 수 없습니다"));
        } catch (RuntimeException e) {
            assert e.getMessage().contains("시스템 코드를 찾을 수 없습니다");
        }
    }
}
