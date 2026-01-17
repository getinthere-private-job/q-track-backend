package dev.dote.qtrack.item;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import dev.dote.qtrack.user.Role;
import dev.dote.qtrack.user.User;
import dev.dote.qtrack.user.UserRepository;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
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
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
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
    }

    @Test
    void findAll_test() throws Exception {
        // given
        Item item1 = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        Item item2 = new Item("ITEM002", "부품2", "부품2 설명", "카테고리2");
        itemRepository.save(item1);
        itemRepository.save(item2);

        // when
        ResultActions result = mvc.perform(
                get("/api/items")
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body.length()").value(2));
    }

    @Test
    void findById_test() throws Exception {
        // given
        Item item = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(item);
        Long itemId = item.getId();

        // when
        ResultActions result = mvc.perform(
                get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("성공"))
                .andExpect(jsonPath("$.body.id").value(itemId.intValue()))
                .andExpect(jsonPath("$.body.code").value("ITEM001"))
                .andExpect(jsonPath("$.body.name").value("부품1"))
                .andExpect(jsonPath("$.body.description").value("부품1 설명"))
                .andExpect(jsonPath("$.body.category").value("카테고리1"));
    }

    @Test
    void create_as_manager_test() throws Exception {
        // given
        ItemRequest.Create request = new ItemRequest.Create("ITEM001", "부품1", "부품1 설명", "카테고리1");
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
                .andExpect(jsonPath("$.body.code").value("ITEM001"))
                .andExpect(jsonPath("$.body.name").value("부품1"));
    }

    @Test
    void create_as_admin_test() throws Exception {
        // given
        ItemRequest.Create request = new ItemRequest.Create("ITEM001", "부품1", "부품1 설명", "카테고리1");
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
                .andExpect(jsonPath("$.body.code").value("ITEM001"));
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
        // given
        Item existingItem = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(existingItem);

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
        // given
        Item item = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(item);
        Long itemId = item.getId();

        ItemRequest.Update request = new ItemRequest.Update("부품1 수정", "수정된 설명", "수정된 카테고리");
        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions result = mvc.perform(
                put("/api/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + managerToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.name").value("부품1 수정"))
                .andExpect(jsonPath("$.body.description").value("수정된 설명"))
                .andExpect(jsonPath("$.body.category").value("수정된 카테고리"));
    }

    @Test
    void update_as_user_forbidden_test() throws Exception {
        // given
        Item item = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(item);
        Long itemId = item.getId();

        ItemRequest.Update request = new ItemRequest.Update("부품1 수정", "수정된 설명", "수정된 카테고리");
        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions result = mvc.perform(
                put("/api/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void delete_as_admin_test() throws Exception {
        // given
        Item item = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(item);
        Long itemId = item.getId();

        // when
        ResultActions result = mvc.perform(
                delete("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + adminToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.id").value(itemId.intValue()));
    }

    @Test
    void delete_as_manager_forbidden_test() throws Exception {
        // given
        Item item = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(item);
        Long itemId = item.getId();

        // when
        ResultActions result = mvc.perform(
                delete("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + managerToken));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void delete_as_user_forbidden_test() throws Exception {
        // given
        Item item = new Item("ITEM001", "부품1", "부품1 설명", "카테고리1");
        itemRepository.save(item);
        Long itemId = item.getId();

        // when
        ResultActions result = mvc.perform(
                delete("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isForbidden());
    }
}
