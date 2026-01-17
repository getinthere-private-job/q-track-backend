package dev.dote.qtrack.user;

import dev.dote.qtrack._core.security.JwtUtil;
import dev.dote.qtrack._core.util.Resp;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<Resp<UserResponse.Signup>> signup(@Valid @RequestBody UserRequest.Signup request) {
        User user = userService.signup(request.username(), request.password(), request.role());
        UserResponse.Signup response = new UserResponse.Signup(user.getId(), user.getUsername(), user.getRole());
        return Resp.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Resp<UserResponse.Login>> login(@Valid @RequestBody UserRequest.Login request) {
        User user = userService.login(request.username(), request.password());
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        UserResponse.Login response = new UserResponse.Login(token, user.getId(), user.getUsername(), user.getRole());
        return Resp.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resp<UserResponse.Get>> findById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponse.Get response = new UserResponse.Get(user.getId(), user.getUsername(), user.getRole());
        return Resp.ok(response);
    }
}
