package dev.dote.qtrack.systemcode;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
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
    void setUp() {
        systemCodeRepository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
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
                .andExpect(jsonPath("$.body.length()").value(3));
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
                .andExpect(jsonPath("$.body[1].codeGroup").value("INDUSTRY_AVERAGE"));
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
