package dev.dote.qtrack.process;

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
    void setUp() {
        processRepository.deleteAll();
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
        Process process1 = new Process("W", "작업", "작업 공정", 1);
        Process process2 = new Process("P", "제조", "제조 공정", 2);
        Process process3 = new Process("검", "검사", "검사 공정", 3);
        processRepository.save(process1);
        processRepository.save(process2);
        processRepository.save(process3);

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
                .andExpect(jsonPath("$.body[2].code").value("검"));
    }

    @Test
    void findById_test() throws Exception {
        // given
        Process process = new Process("W", "작업", "작업 공정", 1);
        processRepository.save(process);
        Long processId = process.getId();

        // when
        ResultActions result = mvc.perform(
                get("/api/processes/" + processId)
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body.id").value(processId.intValue()))
                .andExpect(jsonPath("$.body.code").value("W"))
                .andExpect(jsonPath("$.body.name").value("작업"))
                .andExpect(jsonPath("$.body.description").value("작업 공정"))
                .andExpect(jsonPath("$.body.sequence").value(1));
    }

    @Test
    void findById_not_found_test() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when
        ResultActions result = mvc.perform(
                get("/api/processes/" + nonExistentId)
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.msg", containsString("공정을 찾을 수 없습니다")));
    }
}
