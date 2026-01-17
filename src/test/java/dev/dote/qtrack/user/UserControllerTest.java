package dev.dote.qtrack.user;

import static org.hamcrest.Matchers.containsString;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dote.qtrack._core.security.JwtUtil;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
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
        void setUp() {
                userRepository.deleteAll();
                mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();
        }

        @Test
        void signup_test() throws Exception {
                // given
                UserRequest.Signup request = new UserRequest.Signup("testuser", "password123", Role.USER);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/users/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").exists())
                                .andExpect(jsonPath("$.body.username").value("testuser"))
                                .andExpect(jsonPath("$.body.role").value("USER"));
        }

        @Test
        void signup_duplicate_username_test() throws Exception {
                // given
                User existingUser = new User("testuser", passwordEncoder.encode("password123"), Role.USER);
                userRepository.save(existingUser);

                UserRequest.Signup request = new UserRequest.Signup("testuser", "password456", Role.USER);
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/users/signup")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.msg", containsString("이미 존재하는 사용자명")));
        }

        @Test
        void findById_test() throws Exception {
                // given
                User user = new User("testuser", passwordEncoder.encode("password123"), Role.USER);
                userRepository.save(user);
                Long userId = user.getId();
                String token = jwtUtil.generateToken(user.getId(), user.getRole());

                // when
                ResultActions result = mvc.perform(
                                get("/api/users/" + userId)
                                                .header("Authorization", "Bearer " + token));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.id").value(userId.intValue()))
                                .andExpect(jsonPath("$.body.username").value("testuser"))
                                .andExpect(jsonPath("$.body.role").value("USER"));
        }

        @Test
        void login_success_test() throws Exception {
                // given
                User user = new User("testuser", passwordEncoder.encode("password123"), Role.USER);
                userRepository.save(user);

                UserRequest.Login request = new UserRequest.Login("testuser", "password123");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/users/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.msg").value("성공"))
                                .andExpect(jsonPath("$.body.token").exists())
                                .andExpect(jsonPath("$.body.id").value(user.getId().intValue()))
                                .andExpect(jsonPath("$.body.username").value("testuser"))
                                .andExpect(jsonPath("$.body.role").value("USER"));
        }

        @Test
        void login_wrong_password_test() throws Exception {
                // given
                User user = new User("testuser", passwordEncoder.encode("password123"), Role.USER);
                userRepository.save(user);

                UserRequest.Login request = new UserRequest.Login("testuser", "wrongpassword");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/users/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.msg", containsString("비밀번호가 일치하지 않습니다")));
        }

        @Test
        void login_user_not_found_test() throws Exception {
                // given
                UserRequest.Login request = new UserRequest.Login("nonexistent", "password123");
                String requestBody = om.writeValueAsString(request);

                // when
                ResultActions result = mvc.perform(
                                post("/api/users/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody));

                // then
                result.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.msg", containsString("사용자를 찾을 수 없습니다")));
        }
}
