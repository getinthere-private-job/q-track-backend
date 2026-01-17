package dev.dote.qtrack._core.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import dev.dote.qtrack.user.Role;
import dev.dote.qtrack.user.User;
import dev.dote.qtrack.user.UserRepository;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        user = new User("testuser", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);
        token = jwtUtil.generateToken(user.getId(), user.getRole());
    }

    @Test
    void authenticated_access_success_test() throws Exception {
        // given - 인증된 사용자
        Long userId = user.getId();

        // when
        ResultActions result = mvc.perform(
                get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.id").value(userId.intValue()));
    }

    @Test
    void unauthenticated_access_fail_test() throws Exception {
        // given - 인증되지 않은 요청
        Long userId = user.getId();

        // when
        ResultActions result = mvc.perform(
                get("/api/users/" + userId));

        // then
        result.andExpect(status().isForbidden()); // Spring Security는 인증되지 않은 요청에 대해 403 반환
    }

    @Test
    void signup_permit_all_test() throws Exception {
        // given - permitAll 설정된 엔드포인트
        // when
        ResultActions result = mvc.perform(
                post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"password123\",\"role\":\"USER\"}"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void login_permit_all_test() throws Exception {
        // given - permitAll 설정된 엔드포인트
        // when
        ResultActions result = mvc.perform(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password123\"}"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void invalid_token_access_fail_test() throws Exception {
        // given - 잘못된 토큰
        Long userId = user.getId();
        String invalidToken = "invalid.token.here";

        // when
        ResultActions result = mvc.perform(
                get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + invalidToken));

        // then
        result.andExpect(status().isForbidden()); // Spring Security는 잘못된 토큰에 대해 403 반환
    }
}
