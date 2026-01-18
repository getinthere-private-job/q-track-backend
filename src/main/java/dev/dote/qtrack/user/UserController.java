package dev.dote.qtrack.user;

import dev.dote.qtrack._core.security.JwtUtil;
import dev.dote.qtrack._core.util.Resp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 API
 * - 사용자 회원가입 및 로그인 기능 제공
 * - 사용자 정보 조회 기능
 * - JWT 토큰 기반 인증 처리
 */
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<Resp<UserResponse.Signup>> signup(@Valid @RequestBody UserRequest.Signup request) {
        UserResponse.Signup response = userService.signup(request.username(), request.password(), request.role());
        return Resp.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Resp<UserResponse.Login>> login(@Valid @RequestBody UserRequest.Login request) {
        UserResponse.Get userDto = userService.login(request.username(), request.password());
        String token = jwtUtil.generateToken(userDto.id(), userDto.role());
        UserResponse.Login response = new UserResponse.Login(token, userDto.id(), userDto.username(), userDto.role());
        return Resp.ok(response);
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<Resp<UserResponse.Get>> findById(@PathVariable Long id) {
        UserResponse.Get response = userService.findById(id);
        return Resp.ok(response);
    }
}
